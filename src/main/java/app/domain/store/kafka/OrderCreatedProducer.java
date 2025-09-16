package app.domain.store.kafka;

import app.domain.mongo.StoreCollectionQueryService;
import app.domain.mongo.model.entity.StoreCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final StoreCollectionQueryService storeCollectionQueryService;

    public void publishOrderCompleted(List<RedisCartItem> items, Long totalPrice, String orderId) {
        try {
            String storeId = items.get(0).getStoreId().toString();
            
            StoreCollection store = storeCollectionQueryService.findStoreByStoreKey(storeId)
                .orElse(null);
            
            if (store == null) {
                log.warn("Store not found for storeId: {}", storeId);
                return;
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("storeId", storeId);
            eventData.put("totalPrice", totalPrice);
            eventData.put("category_main", store.getCategoryKeys().get(0));
            eventData.put("category_sub", store.getCategoryKeys().get(1));
            eventData.put("category_item", store.getCategoryKeys().get(2));
            eventData.put("region", store.getRegionName());
            eventData.put("min_order_amount", store.getMinOrderAmount());
            eventData.put("avg_rating", BigDecimal.valueOf(store.getAvgRating()).setScale(2, RoundingMode.HALF_UP).doubleValue());

            String message = objectMapper.writeValueAsString(eventData);
            ProducerRecord<String, String> record = new ProducerRecord<>("dev.order.completed", message);
            record.headers().add("orderId", orderId.getBytes());
            kafkaTemplate.send(record);
            
            log.info("Published order completed event for storeId: {}", storeId);
        } catch (Exception e) {
            log.error("Failed to publish order completed event", e);
        }
    }
}