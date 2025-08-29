package app.domain.store.client;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import app.commonUtil.apiPayload.ApiResponse;
import app.domain.store.model.dto.response.GetUserInfoResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    public ApiResponse<Boolean> isUserExists() {
        String url = userServiceUrl + "/internal/user/exists";

        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
        );

        return response.getBody();
    }

    public ApiResponse<String> getUserName() {
        String url = userServiceUrl + "/internal/user/name";

        ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<String>>() {}
        );

        return response.getBody();
    }

    public ApiResponse<GetUserInfoResponse> getUserInfo(){
        String url = userServiceUrl + "internal/user/info";

        ResponseEntity<ApiResponse<GetUserInfoResponse>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<GetUserInfoResponse>>() {}
        );

        return response.getBody();
    }
}
