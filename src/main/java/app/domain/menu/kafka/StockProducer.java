package app.domain.menu.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStockResult(String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>("stock.result", "");
        record.headers().add("eventType", eventType.getBytes());
        
        kafkaTemplate.send(record);
        log.info("Sent stock result with eventType: {}", eventType);
    }
}