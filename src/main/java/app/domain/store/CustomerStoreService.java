package app.domain.store;

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

		Map<UUID, ReviewClient.RatingSummary> summaries =
			ids.isEmpty() ? Map.of() : reviewClient.fetchRating(ids);

		List<GetStoreListResponse> content = page.getContent().stream()
			.map(store -> {
				var sum = summaries.get(store.getStoreId());
				double avg = (sum != null) ? sum.getAvg() : 0.0;
				return GetStoreListResponse.from(store, avg);
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

		// 2) 리뷰 평균(요약) 조회: 배치 API를 단건으로 재사용
		double avg = 0.0;
		try {
			Map<UUID, ReviewClient.RatingSummary> map =
				reviewClient.fetchRating(List.of(storeId));  // 내부 호출 (X-Internal-Auth 권장)
			var sum = map.get(storeId);
			if (sum != null) {
				avg = sum.getAvg();
			}
		} catch (Exception e) {

		}


		return GetCustomerStoreDetailResponse.from(store, avg);
	}

	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> searchStoresByStatus(
		String keyword, String categoryWord, StoreAcceptStatus status, Pageable pageable) {
		return storeQueryRepository.searchStores(
			keyword, categoryWord, status, pageable
		);
	}
}