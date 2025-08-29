package app.domain.store.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.commonUtil.security.TokenPrincipalParser;
import app.domain.store.service.StoreService;
import app.domain.store.model.dto.response.GetReviewResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.dto.response.StoreOrderListResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Store", description = "가게, 가게 메뉴 관리")
@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class StoreController {

	private final StoreService storeService;
	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;
	private final TokenPrincipalParser tokenPrincipalParser;

	@PostMapping
	public ApiResponse<StoreApproveResponse> createStore(@Valid @RequestBody StoreApproveRequest request, Authentication authentication) {
		validateCreateStoreRequest(request);

		StoreApproveResponse response = storeService.createStore(request, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));

		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_CREATED_SUCCESS, response);
	}

	private void validateCreateStoreRequest(StoreApproveRequest request) {
		if (request.getRegionId() == null) {
			throw new GeneralException(StoreErrorCode.REGION_ID_NULL);
		}
		Region region = regionRepository.findById(request.getRegionId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.REGION_NOT_FOUND));

		if (request.getCategoryId() == null) {
			throw new GeneralException(StoreErrorCode.CATEGORY_ID_NULL);
		}
		if (request.getAddress() == null) {
			throw new GeneralException(StoreErrorCode.ADDRESS_NULL);
		}
		if (request.getStoreName() == null) {
			throw new GeneralException(StoreErrorCode.STORE_NAME_NULL);
		}
		if (request.getMinOrderAmount() == null) {
			throw new GeneralException(StoreErrorCode.MIN_ORDER_AMOUNT_NULL);
		}
		if (request.getMinOrderAmount() < 0) {
			throw new GeneralException(StoreErrorCode.MIN_ORDER_AMOUNT_INVALID);
		}
		if (storeRepository.existsByStoreNameAndRegion(request.getStoreName(), region)) {
			throw new GeneralException(StoreErrorCode.DUPLICATE_STORE_NAME_IN_REGION);
		}
	}

	@PutMapping
	public ApiResponse<StoreInfoUpdateResponse> updateStore(@Valid @RequestBody StoreInfoUpdateRequest request, Authentication authentication) {

		StoreInfoUpdateResponse response = storeService.updateStoreInfo(request, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_UPDATED_SUCCESS, response);
	}

	@DeleteMapping("/{storeId}")
	public ApiResponse<String> deleteStore(@Valid @PathVariable UUID storeId, Authentication authentication) {
		storeService.deleteStore(storeId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus.STORE_DELETED_SUCCESS,
			StoreSuccessStatus.STORE_DELETED_SUCCESS.getMessage());
	}

	@GetMapping("/{storeId}/menu")
	public ApiResponse<MenuListResponse> getStoreMenus(@PathVariable UUID storeId, Authentication authentication) {
		MenuListResponse response = storeService.getStoreMenuList(storeId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus._OK, response);
	}

	@GetMapping("/{storeId}/review")
	public ApiResponse<List<GetReviewResponse>> getStoreReviews(@PathVariable UUID storeId, Authentication authentication) {
		List<GetReviewResponse> response = storeService.getStoreReviewList(storeId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus._OK, response);
	}

	@GetMapping("/{storeId}/order")
	public ApiResponse<StoreOrderListResponse> getStoreOrders(@PathVariable UUID storeId, Authentication authentication) {
		StoreOrderListResponse response = storeService.getStoreOrderList(storeId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus._OK, response);
	}

	@PostMapping("/order/{orderId}/accept")
	public ApiResponse<String> acceptOrder(@PathVariable UUID orderId, Authentication authentication) {
		storeService.acceptOrder(orderId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus.ORDER_ACCEPTED_SUCCESS, "주문 수락이 완료되었습니다.");
	}

	@PostMapping("/order/{orderId}/reject")
	public ApiResponse<String> rejectOrder(@PathVariable UUID orderId, Authentication authentication) {
		storeService.rejectOrder(orderId, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreSuccessStatus.ORDER_REJECTED_SUCCESS, "주문 거절이 완료되었습니다.");
	}
}