package app.domain.store.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.store.client.ReviewClient;
import app.domain.store.model.StoreQueryRepository;
import app.domain.store.model.dto.response.GetCustomerStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerStoreService {

	private final StoreRepository storeRepository;
	private final ReviewClient reviewClient;
	private final StoreQueryRepository storeQueryRepository;

	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> getApprovedStore(Pageable pageable) {

		Page<Store> page = storeRepository.findAllByStoreAcceptStatus(
			StoreAcceptStatus.APPROVE, pageable
		);

		List<UUID> ids = page.getContent().stream()
			.map(Store::getStoreId)
			.toList();

		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse = reviewClient.getStoreReviewAverage(ids);
		if(!storeReviewResponse.isSuccess()){
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}

		List<ReviewClient.StoreReviewResponse> storeReviewResponses =storeReviewResponse.result();

		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap = storeReviewResponses.stream()
			.collect(java.util.stream.Collectors.toMap(
				ReviewClient.StoreReviewResponse::getStoreId,
				review -> review
			));
		
		List<GetStoreListResponse> content = page.getContent().stream()
			.map(store -> {
				ReviewClient.StoreReviewResponse review = reviewMap.get(store.getStoreId());
				long number=(review != null)?review.getNumber():0L;
				double avg = (review != null) ? review.getAverage() : 0.0;
				return GetStoreListResponse.from(store, number,avg);
			})
			.toList();

		Page<GetStoreListResponse> out = new PageImpl<>(content, pageable, page.getTotalElements());
		return PagedResponse.from(out);
	}
	@Transactional(readOnly = true)
	public GetCustomerStoreDetailResponse getApproveStoreDetail(UUID storeId) {
		// 1) 스토어 검증
		Store store = storeRepository
			.findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(storeId, StoreAcceptStatus.APPROVE)
			.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		// 2) 리뷰 평균(요약) 조회
		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse = reviewClient.getStoreReviewAverage(List.of(storeId));
		if(!storeReviewResponse.isSuccess()){
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}

		List<ReviewClient.StoreReviewResponse> storeReviewResponses = storeReviewResponse.result();
		
		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap = storeReviewResponses.stream()
			.collect(java.util.stream.Collectors.toMap(
				ReviewClient.StoreReviewResponse::getStoreId,
				review -> review
			));
		
		ReviewClient.StoreReviewResponse review = reviewMap.get(storeId);
		long number =(review!=null) ? review.getNumber() :0L;
		double avg = (review != null) ? review.getAverage() : 0.0;

		return GetCustomerStoreDetailResponse.from(store, number,avg);
	}

	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> searchStoresByStatus(
		String keyword, String categoryWord, StoreAcceptStatus status, Pageable pageable) {
		return storeQueryRepository.searchStores(
			keyword, categoryWord, status, pageable
		);
	}
}