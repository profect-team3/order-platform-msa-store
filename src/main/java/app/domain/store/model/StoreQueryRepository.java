package app.domain.store.model;

import org.springframework.data.domain.Pageable;

import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.PagedResponse;

public interface StoreQueryRepository {
	PagedResponse<GetStoreListResponse> searchStores(
		String keyword,
		String categoryKeyword,
		StoreAcceptStatus status,
		Pageable pageable
	);
	PagedResponse<GetStoreListResponse> getApprovedStore(Pageable pageable);

	PagedResponse<GetStoreListResponse> getAllStore(StoreAcceptStatus status, Pageable pageable);
}