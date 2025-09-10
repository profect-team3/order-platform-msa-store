package app.domain.store.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderValidProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void sendOrderValidResult(Map<String, Object> headers, Object payload) {
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);
			
			Message<String> message = MessageBuilder
				.withPayload(payloadJson)
				.setHeader("eventType", headers.get("eventType"))
				.setHeader("orderId", headers.get("orderId"))
				.build();
			
			kafkaTemplate.send("order-valid-result", message.getPayload());
		} catch (JsonProcessingException e) {
			log.error("Could not serialize payload for orderId: {}", headers.get("orderId"), e);
		}
	}
}