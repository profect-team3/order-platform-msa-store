package app.domain.store.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.PagedResponse;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.store.client.ReviewClient;
import app.domain.store.model.StoreQueryRepository;
import app.domain.store.model.dto.response.GetCustomerStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
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
		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse;
		try{
			storeReviewResponse = reviewClient.getStoreReviewAverage(ids);
		} catch (HttpClientErrorException | HttpServerErrorException e){
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
		Store store = storeRepository
			.findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(storeId, StoreAcceptStatus.APPROVE)
			.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse;
		try{
			storeReviewResponse = reviewClient.getStoreReviewAverage(List.of(storeId));
		} catch (HttpClientErrorException | HttpServerErrorException e){
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