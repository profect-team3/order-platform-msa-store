package app.domain.menu.kafka;

import app.domain.menu.model.entity.Stock;
import app.domain.menu.model.repository.StockRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final StockRepository stockRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String LUA_SCRIPT = """
        local keys = KEYS
        local quantities = ARGV
        
        for i = 1, #keys do
            local stock = redis.call('GET', keys[i])
            if not stock or tonumber(stock) < tonumber(quantities[i]) then
                return 0
            end
        end
        
        for i = 1, #keys do
            redis.call('DECRBY', keys[i], quantities[i])
        end
        
        return 1
        """;

    @Transactional
    public void processStockRequest(List<Map<String, Object>> stockRequests,String headerOrderId) {
        try {
            List<String> keys = stockRequests.stream()
                    .map(req -> "stock:" + req.get("menuId"))
                    .collect(Collectors.toList());

            List<String> quantities = stockRequests.stream()
                    .map(req -> String.valueOf(req.get("quantity")))
                    .collect(Collectors.toList());

            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, keys, quantities.toArray());

            if (result != null && result == 1L) {
                updateDatabaseStock(stockRequests);
                eventPublisher.publishEvent(new StockResultEvent(this,headerOrderId, "success", ""));
            } else {
                eventPublisher.publishEvent(new StockResultEvent(this,headerOrderId, "fail", StoreMenuErrorCode.OUT_OF_STOCK.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error processing stock request", e);
            eventPublisher.publishEvent(new StockResultEvent(this,headerOrderId, "fail", e.getMessage()));
        }
    }

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 300, multiplier = 2)
    )
    @Transactional
    public void updateDatabaseStock(List<Map<String, Object>> stockRequests) {
        List<UUID> menuIds = stockRequests.stream()
                .map(req -> UUID.fromString(req.get("menuId").toString()))
                .collect(Collectors.toList());

        Map<UUID, Stock> stockMap = stockRepository.findByMenuMenuIdIn(menuIds).stream()
                .collect(Collectors.toMap(stock -> stock.getMenu().getMenuId(), stock -> stock));

        stockRequests.forEach(req -> {
            UUID menuId = UUID.fromString(req.get("menuId").toString());
            Integer quantity = Integer.valueOf(req.get("quantity").toString());
            Stock stock = stockMap.get(menuId);
            if (stock != null) {
                stock.setStock(stock.getStock() - quantity);
            }
        });
    }

    @Recover
    public void recoverUpdateDatabaseStock(ObjectOptimisticLockingFailureException e, List<Map<String, Object>> stockRequests) {
        log.error("DB 재고 업데이트 재시도 모두 실패: {}", stockRequests, e);
        throw e;
    }
}