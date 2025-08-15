package app.domain.batch.model.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.sql.Timestamp;

@Getter
@Setter
public class StoreMenuDto {
	private UUID storeId;
	private Long userId;
	private String storeName;
	private String description;
	private Long minOrderAmount;
	private String address;
	private String phoneNumber;
	private String storeAcceptStatus;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	// 조인된 데이터
	private String regionName;
	private String regionFullName;
	private String categoryName;
	private String menuJson; // 메뉴 정보를 JSON 문자열로 저장
	private Double avgRating; // 리뷰 평균 평점
}