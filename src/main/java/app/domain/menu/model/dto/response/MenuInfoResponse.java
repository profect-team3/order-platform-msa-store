package app.domain.menu.model.dto.response;

import app.domain.menu.model.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuInfoResponse {
	private UUID menuId;
	private String name;
	private Long price;

	public static MenuInfoResponse from(Menu menu) {
		return MenuInfoResponse.builder()
			.menuId(menu.getMenuId())
			.name(menu.getName())
			.price(menu.getPrice())
			.build();
	}
}
