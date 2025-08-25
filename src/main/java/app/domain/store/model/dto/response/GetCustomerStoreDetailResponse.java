package app.domain.store.model.dto.response;

import java.util.UUID;

import app.domain.store.model.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerStoreDetailResponse {

	private UUID storeId;
	private String storeName;
	private String description;
	private String address;
	private String phoneNumber;
	private long minOrderAmount;
	private String categoryName;
	private long reviewNumber;
	private double averageRating;

	public GetCustomerStoreDetailResponse() {
	}

	public GetCustomerStoreDetailResponse(UUID storeId, String storeName, String description, String address, String phoneNumber, long minOrderAmount, String categoryName,long reviewNumber ,double averageRating) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.description = description;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.minOrderAmount = minOrderAmount;
		this.categoryName = categoryName;
		this.reviewNumber=reviewNumber;
		this.averageRating = averageRating;
	}

	public static GetCustomerStoreDetailResponse from(Store store, long reviewNumber,double avgRating) {
		return GetCustomerStoreDetailResponse.builder()
			.storeId(store.getStoreId())
			.storeName(store.getStoreName())
			.description(store.getDescription())
			.address(store.getAddress())
			.phoneNumber(store.getPhoneNumber())
			.minOrderAmount(store.getMinOrderAmount())
			.categoryName(store.getCategory().getCategoryName())
			.reviewNumber(reviewNumber)
			.averageRating(avgRating)
			.build();
	}
}