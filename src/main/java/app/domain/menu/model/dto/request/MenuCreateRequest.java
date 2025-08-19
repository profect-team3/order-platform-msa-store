package app.domain.menu.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MenuCreateRequest {
	@NotNull
	private UUID storeId;
	@NotNull
	private String name;
	@NotNull
	private Long price;
	private String description;
	@NotNull
	private UUID categoryId;
}
