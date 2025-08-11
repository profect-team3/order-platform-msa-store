package app.domain.store.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "store-service", url = "http://localhost:8082")
public interface InternalStoreClient {

    @GetMapping("/internal/store/{storeId}/exists")
    Boolean isStoreExists(@PathVariable("storeId") UUID storeId);

    @GetMapping("/internal/store/{storeId}/owner/{userId}")
    Boolean isStoreOwner(@PathVariable("storeId") UUID storeId, @PathVariable("userId") Long userId);
}
