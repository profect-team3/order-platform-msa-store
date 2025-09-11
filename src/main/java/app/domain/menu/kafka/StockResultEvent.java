package app.domain.menu.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockResultEvent {
    private final String orderId;
    private final String status;
    private final String message;
}