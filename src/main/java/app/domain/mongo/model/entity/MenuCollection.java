package app.domain.mongo.model.entity;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MenuCollection {
	@Id
	private String menuId;
	private String name;
	private Integer price;
	private String description;
	private String category;
	private boolean isHidden = false;
}