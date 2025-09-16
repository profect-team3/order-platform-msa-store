package app.domain.menu.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockConsumer {

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock.request", groupId = "stock.group")
    public void handleStockRequest(String message, @Header(value = "orderId", required = false) String headerOrderId) {
        try {
            List<Map<String, Object>> stockRequests = objectMapper.readValue(message, new TypeReference<List<Map<String, Object>>>() {});
            log.info("Received stock request: {}", stockRequests);
            stockService.processStockRequest(stockRequests,headerOrderId);
        } catch (Exception e) {
            log.error("Failed to parse stock request message: {}", message, e);
        }
    }

    @KafkaListener(topics = "refund.request",groupId= "stock.refund.group")
    public void handleRefundRequest(String message, @Header(value = "orderId", required = false) String headerOrderId) {
        try {
            List<Map<String, Object>> refundRequests = objectMapper.readValue(message, new TypeReference<List<Map<String, Object>>>() {});
            log.info("Received refund request: {}", refundRequests);
            stockService.processStockRefund(refundRequests);
        } catch (Exception e) {
            log.error("Failed to parse refund request message: {}", message, e);
        }
    }
}