package app.domain.store.kafka;

import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
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

	@KafkaListener(topics="order.valid.request",groupId = "order.valid.consumer")
	public void consume(String message){
		Map<String, Object> event;
		try {
			event = objectMapper.readValue(message, Map.class);
		} catch (JsonProcessingException e) {
			Map<String, Object> headers = new HashMap<>();
			headers.put("eventType", "fail");
			headers.put("orderId", null);
			Map<String, Object> errorPayload = new HashMap<>();
			errorPayload.put("errorMessage", "Parse error");
			orderValidProducer.sendOrderValidResult(headers, errorPayload);
			return;
		}

		try {
			Long userId = ((Number) event.get("userId")).longValue();
			UUID orderId = UUID.fromString((String) event.get("orderId"));
			Long totalPrice = ((Number) event.get("totalPrice")).longValue();

			List<RedisCartItem> items = getCartFromRedis(userId);

			if(items.isEmpty()){
				throw new GeneralException(StoreErrorCode.CART_NOT_FOUND);
			}

			if(!storeRepository.existsByStoreId(items.get(0).getStoreId())){
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

			if (calculatedTotalPrice != totalPrice) {
				throw new GeneralException(StoreErrorCode.INVALID_TOTAL_PRICE);
			}
			// --- End Validation ---

			// --- Success Case ---
			List<Map<String, Object>> menuList = items.stream()
				.map(item -> {
					Menu menu = menuMap.get(item.getMenuId());
					Map<String, Object> menuInfo = new HashMap<>();
					menuInfo.put("menuId", menu.getMenuId());
					menuInfo.put("menuName", menu.getName());
					menuInfo.put("price", menu.getPrice());
					menuInfo.put("quantity", item.getQuantity());
					return menuInfo;
				})
				.collect(Collectors.toList());

			Map<String, Object> headers = new HashMap<>();
			headers.put("eventType", "success");
			headers.put("orderId", orderId);
			orderValidProducer.sendOrderValidResult(headers, menuList);

		} catch (GeneralException e) {
			// --- Failure Case ---
			UUID orderId = null;
			try {
				orderId = UUID.fromString((String) event.get("orderId"));
			} catch (Exception ex) {
				// orderId parsing failed, keep null
			}
			Map<String, Object> headers = new HashMap<>();
			headers.put("eventType", "fail");
			headers.put("orderId", orderId);
			Map<String, Object> errorPayload = new HashMap<>();
			errorPayload.put("errorMessage", e.getMessage());
			orderValidProducer.sendOrderValidResult(headers, errorPayload);
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
