package app.domain.store.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOrderListResponse {

    private UUID storeId;
    private List<StoreOrderDetail> orderList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreOrderDetail {
        private UUID orderId;
        private Long customerId;
        private String customerName;
        private Long totalPrice;
        private String orderStatus;
        private LocalDateTime orderedAt;
    }
}