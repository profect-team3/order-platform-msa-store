package app.domain.store.kafka;

import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.kafka.dto.OrderCreateRequestEvent;
import app.domain.store.kafka.dto.RedisCartItem;
import app.domain.store.kafka.dto.MenuInfoResponse;
import app.domain.store.kafka.dto.OrderValidResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderValidConsumer {
	private final ObjectMapper objectMapper;
	private final RedisTemplate<String, String> redisTemplate;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;
	private final OrderValidProducer orderValidProducer;

	@KafkaListener(topics="order-valid-request",groupId = "order-valid-consumer")
	public void consume(String message){
		OrderCreateRequestEvent event;
		try {
			event = objectMapper.readValue(message, OrderCreateRequestEvent.class);
		} catch (JsonProcessingException e) {
			OrderValidResponse response = OrderValidResponse.builder()
				.orderId(null)
				.eventType("fail")
				.build();
			orderValidProducer.sendOrderValidResult(response);
			return;
		}

		try {
			List<RedisCartItem> items = getCartFromRedis(event.getUserId());

			if(items.isEmpty()){
				throw new GeneralException(StoreErrorCode.CART_NOT_FOUND);
			}

			if(!storeRepository.existsByStoreId(event.getStoreId())){
				throw new GeneralException(StoreErrorCode.STORE_NOT_FOUND);
			}

			List<UUID> menuIds = items.stream()
				.map(RedisCartItem::getMenuId)
				.distinct()
				.collect(Collectors.toList());
			List<Menu> menus = menuRepository.findAllById(menuIds);

			if (menus.size() != menuIds.size()) {
				throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
			}

			Map<UUID, Menu> menuMap = menus.stream().collect(Collectors.toMap(Menu::getMenuId, Function.identity()));

			long calculatedTotalPrice = items.stream()
				.mapToLong(item -> menuMap.get(item.getMenuId()).getPrice() * item.getQuantity())
				.sum();

			if (calculatedTotalPrice != event.getTotalPrice()) {
				throw new GeneralException(StoreErrorCode.INVALID_TOTAL_PRICE);
			}
			// --- End Validation ---

			// --- Success Case ---
			Map<UUID, MenuInfoResponse> menuInfos = items.stream()
				.collect(Collectors.toMap(
					RedisCartItem::getMenuId,
					item -> {
						Menu menu = menuMap.get(item.getMenuId());
						return new MenuInfoResponse(menu.getName(), menu.getPrice(), item.getQuantity());
					}
				));

			OrderValidResponse response = OrderValidResponse.builder()
				.orderId(event.getOrderId())
				.eventType("success")
				.menuInfos(menuInfos)
				.build();
			orderValidProducer.sendOrderValidResult(response);

		} catch (GeneralException e) {
			// --- Failure Case ---
			OrderValidResponse response = OrderValidResponse.builder()
				.orderId(event.getOrderId())
				.eventType("fail")
				.build();
			orderValidProducer.sendOrderValidResult(response);
		}
	}

	public List<RedisCartItem> getCartFromRedis(Long userId) {
		try {
			String key = "cart:" + userId;

			String keyType = redisTemplate.type(key).code();
			if ("string".equals(keyType)) {
				return new ArrayList<>();
			}

			return redisTemplate.opsForHash().values(key).stream()
				.map(value -> {
					try {
						return objectMapper.readValue((String)value, RedisCartItem.class);
					} catch (JsonProcessingException e) {
						throw new GeneralException(StoreErrorCode.CART_ITEM_PARSE_FAILED);
					}
				})
				.collect(Collectors.toList());
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			throw new GeneralException(StoreErrorCode.CART_REDIS_LOAD_FAILED);
		}
	}


}
