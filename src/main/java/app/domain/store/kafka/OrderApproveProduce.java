package app.domain.store.kafka;

import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApproveProduce {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendOrderApproveResult(UUID orderId,String result) {
		try {
			ProducerRecord<String, String> record = new ProducerRecord<>("order.approve.result",null);
			record.headers().add(new RecordHeader("eventType", result.getBytes()));
			record.headers().add(new RecordHeader("orderId", orderId.toString().getBytes()));

			kafkaTemplate.send(record);
		} catch (GeneralException e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}
