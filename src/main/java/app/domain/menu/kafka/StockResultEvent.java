package app.domain.menu.kafka;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StockResultEvent extends ApplicationEvent {
    private final String orderId;
    private final String status;
    private final String message;

    public StockResultEvent(Object source, String orderId, String status, String message) {
        super(source);
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}