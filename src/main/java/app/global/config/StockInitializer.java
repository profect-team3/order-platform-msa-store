package app.global.config;

import app.domain.menu.model.entity.Stock;
import app.domain.menu.model.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing Redis stock data...");
        
        List<Stock> stocks = stockRepository.findAll();
        
        for (Stock stock : stocks) {
            String key = "stock:" + stock.getMenu().getMenuId();
            redisTemplate.opsForValue().set(key, String.valueOf(stock.getStock()));
        }
        
        log.info("Redis stock initialization completed. {} items loaded.", stocks.size());
    }
}