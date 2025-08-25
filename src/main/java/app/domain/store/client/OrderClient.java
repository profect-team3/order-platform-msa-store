package app.domain.store.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import app.domain.store.model.dto.response.OrderInfo;
import app.domain.store.model.dto.response.StoreOrderInfo;
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;

    @Value("${order.service.url:http://localhost:8084}")
    private String orderServiceUrl;

    public ApiResponse<List<StoreOrderInfo>> getOrdersByStoreId(UUID storeId){
        String url = orderServiceUrl + "/internal/order/store/"+storeId;
        ResponseEntity<ApiResponse<List<StoreOrderInfo>>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<List<StoreOrderInfo>>>() {}
        );
        return response.getBody();
    }

    public ApiResponse<OrderInfo> getOrderInfo(UUID orderId){
        String url = orderServiceUrl + "/internal/order/"+orderId;
        ResponseEntity<ApiResponse<OrderInfo>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<OrderInfo>>() {}
        );
        return response.getBody();
    }

    public ApiResponse<String> updateOrderStatus(UUID orderId,String status){
        String url = orderServiceUrl + "/internal/order/"+orderId+"/status";
        HttpEntity<String> requestEntity=new HttpEntity<>(status);
        ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<ApiResponse<String>>() {}
        );
        return response.getBody();
    }

}
