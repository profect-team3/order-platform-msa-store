package app.domain.store.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import app.domain.store.client.ReviewClient;
import app.domain.store.client.UserClient;
import app.domain.store.model.StoreQueryRepository;
import app.domain.store.model.dto.response.GetStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.dto.response.GetUserInfoResponse;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

	private final StoreRepository storeRepository;
	private final StoreQueryRepository storeQueryRepository;
	private final ReviewClient reviewClient;
	private final UserClient userClient;

	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> getAllStore(StoreAcceptStatus status, Pageable pageable) {
		return storeQueryRepository.getAllStore(status, pageable);
	}

	@Transactional(readOnly = true)
	public GetStoreDetailResponse getStoreDetail(UUID storeId) {
		Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		List<UUID> storeIds=List.of(store.getStoreId());
		ApiResponse<List<ReviewClient.StoreReviewResponse>> getStoreListResponse;
		try{
			getStoreListResponse=reviewClient.getStoreReviewAverage(storeIds);
		} catch(HttpServerErrorException | HttpClientErrorException e){
			log.error("Review Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}

		Double avgRating = getStoreListResponse.result().get(0).getAverage();
		ApiResponse<GetUserInfoResponse> getUserInfoResponse;
		try{
			getUserInfoResponse = userClient.getUserInfo();
		} catch (HttpServerErrorException | HttpClientErrorException e){
			throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
		}

		GetUserInfoResponse userInfo = getUserInfoResponse.result();

		return GetStoreDetailResponse.from(store, userInfo.getUserId(), userInfo.getEmail(),
			userInfo.getUsername(),userInfo.getRealName(),
			userInfo.getPhoneNumber(),avgRating);
	}

	@Transactional
	public String approveStore(UUID storeId, StoreAcceptStatus status) {
		Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));
		if (status == store.getStoreAcceptStatus()) {
			throw new GeneralException(StoreErrorCode.INVALID_STORE_STATUS);
		}
		store.updateAcceptStatus(status);
		return store.getStoreName() + "의 상태가 변경 되었습니다.";
	}

	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> searchStore(String keyword,String category,StoreAcceptStatus status,
		Pageable pageable) {
		return storeQueryRepository.searchStores(keyword,category ,status,pageable);
	}

}