package app.domain.store.internal;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import app.domain.store.status.StoreSuccessStatus;
import app.global.apiPayload.ApiResponse;
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
	public ApiResponse<Boolean> isStoreExists(@PathVariable UUID storeId) {
		Boolean result =internalStoreService.isStoreExists(storeId);
		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_EXISTS,result);
	}

	@Operation(
		summary = "가게와 사용자 일치 확인",
		description = "storeId와 userId를 받아서 일치 여부 확인")
	@GetMapping("/{storeId}/owner/{userId}")
	public ApiResponse<Boolean> isStoreOwner(
		@PathVariable UUID storeId,
		@PathVariable Long userId) {
		Boolean result=internalStoreService.isStoreOwner(storeId, userId);
		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_OWNER_SUCCESS,result);
	}

	@GetMapping("/{storeId}/name")
	public ApiResponse<String> getStoreName(@PathVariable UUID storeId){
		String name =internalStoreService.getStoreName(storeId);
		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_NAME_FETCHED,name);
	}

}
