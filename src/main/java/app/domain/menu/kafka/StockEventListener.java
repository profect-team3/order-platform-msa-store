package app.domain.menu.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventListener {

    private final StockProducer stockProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockResult(StockResultEvent event) {
        stockProducer.sendStockResult(event.getOrderId(), event.getStatus(), event.getMessage());
    }
}