package app.domain.store.kafka;

import app.domain.store.kafka.dto.OrderValidResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderValidProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void sendOrderValidResult(OrderValidResponse response) {
		try {
			String payload = objectMapper.writeValueAsString(response);
			kafkaTemplate.send("order-valid-result", payload);
		} catch (JsonProcessingException e) {
			log.error("Could not serialize OrderValidResponse for orderId: {}", response.getOrderId(), e);
		}
	}
}