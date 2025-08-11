package app.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 임시 FeignClient 스텁
@FeignClient(name = "reviewService", url = "http://localhost:8086") // 실제 서비스 URL로 변경 필요
public interface ReviewClient {

    @GetMapping("/review/store/{storeId}")
    List<GetReviewResponse> getReviewsByStoreId(@PathVariable("storeId") UUID storeId);
}
