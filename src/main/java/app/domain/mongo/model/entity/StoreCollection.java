package app.domain.mongo.model.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "stores")
@CompoundIndex(name = "store_search_index", def = "{'categoryKeys': 1, 'regionName': 1, 'isActive': 1}")
public class StoreCollection {
	private Long userId;
	private String storeKey;
	private String storeName;
	private String description;
	private List<String> categoryKeys;
	private Double avgRating;
	private Long reviewCount;
	private String phoneNumber;
	private Long minOrderAmount;
	private String address;
	private String regionName;
	private String regionFullName;
	private String storeAcceptStatus;
	private Boolean isActive;

	private Date createdAt;
	@Indexed
	private Date updatedAt;
	private Date deletedAt;

	@Field("version")
	private Long version;
	private List<MenuCollection> menus;

}