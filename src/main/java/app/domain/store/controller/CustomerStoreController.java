package app.domain.store.controller;

import static org.springframework.data.domain.Sort.Direction.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.store.service.CustomerStoreService;
import app.domain.store.model.dto.response.GetCustomerStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreSuccessStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store/customer/store")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자의 가게 조회")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerStoreController {

	private final CustomerStoreService customerStoreService;


	@GetMapping
	@Operation(
		summary = "승인이 허용된 가게 목록 조회",
		description = "가게 목록을 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬할 수 있습니다.")
	public ApiResponse<PagedResponse<GetStoreListResponse>> getApprovedStoreList(
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
		return ApiResponse.onSuccess(StoreSuccessStatus.CUSTOMER_GET_STORE_LIST_OK,
			customerStoreService.getApprovedStore(pageable));
	}


	@GetMapping("/{storeId}")
	@Operation(
		summary = "승인이 허용된 가게 상세 조회",
		description = "가게 상세 목록을 조회 합니다")
	public ApiResponse<GetCustomerStoreDetailResponse> getApprovedStoreDetail(@PathVariable UUID storeId) {
		return ApiResponse.onSuccess(StoreSuccessStatus.CUSTOMER_GET_STORE_DETAIL_OK,
			customerStoreService.getApproveStoreDetail(storeId));
	}


	@GetMapping("/search")
	@Operation(
		summary = "가게 목록 검색",
		description = "가게를 키워드에 따라 검색 합니다 ")
	public ApiResponse<PagedResponse<GetStoreListResponse>> searchApprovedStore(
		@RequestParam String keyword,String categoryWord,
		@PageableDefault(size = 10) Pageable pageable) {
		return ApiResponse.onSuccess(StoreSuccessStatus.CUSTOMER_SEARCH_STORE_OK,
			customerStoreService.searchStoresByStatus(keyword,categoryWord, StoreAcceptStatus.APPROVE, pageable));
	}
}