package app.domain.batch.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.sql.Timestamp;

@Getter
@Setter
public class BulkDto {
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
	private Timestamp deletedAt;

	private String regionName;
	private String regionFullName;
	private String categoryName;
	private String menuJson;
	private Long reviewCount;
	private Double avgRating;

}
