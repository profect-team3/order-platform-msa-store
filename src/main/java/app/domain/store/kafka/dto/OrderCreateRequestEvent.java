package app.domain.store.kafka.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestEvent {
    private UUID orderId;
    private Long userId;
    private UUID storeId;
    private Long totalPrice;
}
