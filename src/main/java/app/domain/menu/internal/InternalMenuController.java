package app.domain.menu.internal;

import app.domain.menu.model.dto.response.MenuInfoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/menus")
@RequiredArgsConstructor
@Tag(name = "내부 API", description = "내부 API")
public class InternalMenuController {

	private final InternalMenuService internalMenuService;

	@PostMapping("/batch")
	public List<MenuInfoResponse> getMenuInfoList(@RequestBody List<UUID> menuIds) {
		return internalMenuService.getMenuInfoList(menuIds);
	}
}
