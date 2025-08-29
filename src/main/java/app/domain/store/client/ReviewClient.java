package app.domain.store.client;

import app.commonUtil.apiPayload.ApiResponse;
import app.domain.store.model.dto.response.GetReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ReviewClient {

	private final RestTemplate restTemplate;

	@Value("${review.service.url:http://localhost:8086}")
	private String reviewServiceUrl;

	public ApiResponse<List<StoreReviewResponse>> getStoreReviewAverage(List<UUID> storeIds) {
		String url = reviewServiceUrl + "/internal/review/average";

		HttpEntity<List<UUID>> requestEntity = new HttpEntity<>(storeIds);
		
		ResponseEntity<ApiResponse<List<StoreReviewResponse>>> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			requestEntity,
			new ParameterizedTypeReference<ApiResponse<List<StoreReviewResponse>>>() {}
		);

		return response.getBody();
	}

	public ApiResponse<List<GetReviewResponse>> getReviewsByStoreId(UUID storeId){
		String url = reviewServiceUrl + "/internal/review/"+storeId;
		ResponseEntity<ApiResponse<List<GetReviewResponse>>> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<ApiResponse<List<GetReviewResponse>>>() {}
		);
		return response.getBody();
	}

	@Data
	@AllArgsConstructor
	public static class StoreReviewResponse {
		private UUID storeId;
		private Long number;
		private Double average;
	}
}