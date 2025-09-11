package app.domain.menu.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendStockResult(String headerOrderId, String eventType, String message) {
        String value = null;
        
        if ("fail".equals(eventType) && message != null && !message.isEmpty()) {
            try {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("errorMessage", message);
                value = objectMapper.writeValueAsString(errorMap);
            } catch (Exception e) {
                log.error("Failed to serialize error message", e);
            }
        }
        
        ProducerRecord<String, Object> record = new ProducerRecord<>("stock.result", value);
        record.headers().add("eventType", eventType.getBytes());
        record.headers().add("orderId", headerOrderId.getBytes());
        
        kafkaTemplate.send(record);
        log.info("Sent stock result with eventType: {}", eventType);
    }
}