package app.domain.mongo.model.entity;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MenuCollection {
	private String menuId;
	private String name;
	private Integer price;
	private String description;
	private String category;
	private boolean isHidden = false;
}