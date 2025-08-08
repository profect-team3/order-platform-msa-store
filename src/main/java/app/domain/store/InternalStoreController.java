package app.domain.store;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/store")
@RequiredArgsConstructor
@Tag(name = "내부 API", description = "내부 API")
public class InternalStoreController {

	private final InternalStoreService internalStoreService;

	@GetMapping("/{storeId}/exists")
	@Operation(
		summary = "가게 존재여부 확인",
		description = "storeId를 받아서 가게 존재 여부 확인")
	public Boolean isStoreExists(@PathVariable UUID storeId) {
		return internalStoreService.isStoreExists(storeId);
	}

	@Operation(
		summary = "가게와 사용자 일치 확인",
		description = "storeId와 userId를 받아서 일치 여부 확인")
	@GetMapping("/{storeId}/owner/{userId}")
	public Boolean isStoreOwner(
		@PathVariable UUID storeId,
		@PathVariable Long userId) {
		return internalStoreService.isStoreOwner(storeId, userId);
	}
}
