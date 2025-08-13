package app.domain.store.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    public ApiResponse<Boolean> isUserExists(Long userId) {
        String url = userServiceUrl + "/internal/user/"+userId+"/exists";

        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
        );

        return response.getBody();
    }

    public ApiResponse<String> getUserName(Long userId) {
        String url = userServiceUrl + "/internal/user/"+userId+"/name";

        ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<String>>() {}
        );

        return response.getBody();
    }

}
