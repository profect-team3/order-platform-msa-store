package app.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "http://localhost:8082") // 실제 서비스 URL로 변경?
public interface OrderClient {

    @GetMapping("/orders/store/{storeId}")
    List<StoreOrderInfo> getOrdersByStoreId(@PathVariable("storeId") UUID storeId);

    @GetMapping("/orders/{orderId}/info")
    OrderInfo getOrderInfo(@PathVariable("orderId") UUID orderId);

    @PutMapping("/orders/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") UUID orderId, @RequestParam("status") String status);
}
