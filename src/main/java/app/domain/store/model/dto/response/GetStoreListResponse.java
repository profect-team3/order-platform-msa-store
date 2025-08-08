package app.domain.store.model.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreListResponse {

	private UUID storeId;
	private String storeName;
	private String address;
	private long minOrderAmount;
	private double averageRating;

	public GetStoreListResponse() {
	}

	public GetStoreListResponse(UUID storeId, String storeName, String address, long minOrderAmount, double averageRating) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.address = address;
		this.minOrderAmount = minOrderAmount;
		this.averageRating = averageRating;
	}

	public static GetStoreListResponse from(Store store, double averageRating) {
		return GetStoreListResponse.builder()
			.storeId(store.getStoreId())
			.storeName(store.getStoreName())
			.address(store.getAddress())
			.minOrderAmount(store.getMinOrderAmount())
			.averageRating(averageRating)
			.build();
	}
}