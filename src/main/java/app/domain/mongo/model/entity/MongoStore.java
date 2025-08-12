package app.domain.mongo.model.entity;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stores")
public class MongoStore {
	@Id
	private String id;
	private Long userId;
	private String name;
	private String description;
	private String category;
	private Double avgRating;
	private String phonNumber;
	private Long minOrderAmount;
	private String regionName;
	private String regionFullName;
}