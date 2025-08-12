package app.domain.mongo.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "menus")
public class MongoMenu {
	@Id
	private String menuId;
	private String storeId;
	private String name;
	private Integer price;
	private String description;
}