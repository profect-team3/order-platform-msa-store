package app.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 임시 FeignClient 스텁
@FeignClient(name = "user-service", url = "http://localhost:8081") // 실제 서비스 URL로 변경 필요
public interface UserClient {

    @GetMapping("/user/{userId}/exists")
    Boolean isUserExists(@PathVariable("userId") Long userId);

    @GetMapping("/user/{userId}/name")
    String getUserName(@PathVariable("userId") Long userId);
}
