package app.domain.store.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewClient {

	private final RestTemplate restTemplate;

	private static final String REVIEW_SERVICE_URL = "http://review";

	public Map<UUID, RatingSummary> fetchRating(List<UUID> storeIds) {
		if (storeIds == null || storeIds.isEmpty()) {
			return Collections.emptyMap();
		}

		String ids = storeIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));

		String url = UriComponentsBuilder
			.fromHttpUrl(REVIEW_SERVICE_URL + "/internal/ratings")
			.queryParam("storeIds", ids)
			.toUriString();

		Map<UUID, RatingSummary> response =
			restTemplate.getForObject(url, Map.class);

		return Optional.ofNullable(response)
			.orElseGet(Collections::emptyMap);
	}
	public RatingSummary fetchRating(UUID storeId) {
		var url = REVIEW_SERVICE_URL + "/internal/ratings" + storeId;
		var resp = restTemplate.getForEntity(url, RatingSummary.class);
		return Objects.requireNonNullElseGet(resp.getBody(), () -> new RatingSummary());
	}

	@Data
	public static class RatingSummary {
		private double avg;
		private long count;
	}
}