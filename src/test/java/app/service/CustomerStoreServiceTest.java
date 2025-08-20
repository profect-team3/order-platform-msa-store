package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import app.domain.menu.model.entity.Category;
import app.domain.store.client.ReviewClient;
import app.domain.store.model.StoreQueryRepository;
import app.domain.store.model.dto.response.GetCustomerStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.service.CustomerStoreService;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.code.status.SuccessStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class CustomerStoreServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private ReviewClient reviewClient;

	@InjectMocks
	private CustomerStoreService customerStoreService;

	UUID storeId = UUID.randomUUID();

	@Test
	@DisplayName("승인된 가게 목록 조회 성공")
	void getApprovedStore_success() {
		// given
		UUID storeId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		Store mockStore = mock(Store.class);
		when(mockStore.getStoreId()).thenReturn(storeId);

		Page<Store> storePage = new PageImpl<>(List.of(mockStore), pageable, 1);
		when(storeRepository.findAllByStoreAcceptStatus(StoreAcceptStatus.APPROVE,pageable)).thenReturn(storePage);
		ReviewClient.StoreReviewResponse reviewResponse = new ReviewClient.StoreReviewResponse(storeId, 10L,4.5);
		when(reviewClient.getStoreReviewAverage(anyList()))
			.thenReturn(ApiResponse.onSuccess(SuccessStatus._OK, List.of(reviewResponse)));

		// when
		PagedResponse<GetStoreListResponse> result = customerStoreService.getApprovedStore(pageable);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getStoreId()).isEqualTo(storeId);
		assertThat(result.getContent().get(0).getAverageRating()).isEqualTo(4.5);
	}

	@Test
	@DisplayName("가게 상세 조회 성공")
	void getApproveStoreDetail_success() {
		UUID storeId = UUID.randomUUID();
		List<UUID> storeIds=List.of(storeId);

		// given
		Store store = mock(Store.class);
		Category category = mock(Category.class); // 필요한 필드 추가
		given(category.getCategoryName()).willReturn("족발");
		given(store.getCategory()).willReturn(category);
		given(store.getStoreId()).willReturn(storeId);
		given(storeRepository.findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(storeId, StoreAcceptStatus.APPROVE))
			.willReturn(Optional.of(store));
		ReviewClient.StoreReviewResponse reviewResponse = new ReviewClient.StoreReviewResponse(storeId, 10L,4.5);
		when(reviewClient.getStoreReviewAverage(anyList()))
			.thenReturn(ApiResponse.onSuccess(SuccessStatus._OK, List.of(reviewResponse)));


		// when
		GetCustomerStoreDetailResponse response = customerStoreService.getApproveStoreDetail(storeId);

		// then
		assertThat(response.getStoreId()).isEqualTo(storeId);
		assertThat(response.getAverageRating()).isEqualTo(4.5);
		assertThat(response.getCategoryName()).isEqualTo("족발");
	}

	@Test
	@DisplayName("가게 상세 조회 실패 - 존재하지 않음")
	void getApproveStoreDetail_fail_notFound() {
		// given
		given(storeRepository.findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(storeId, StoreAcceptStatus.APPROVE))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> customerStoreService.getApproveStoreDetail(storeId))
			.isInstanceOf(GeneralException.class)
			.satisfies(ex -> {
				GeneralException ge = (GeneralException)ex;
				assertThat(((GeneralException)ex).getCode()).isEqualTo(ErrorStatus.STORE_NOT_FOUND);
			});
	}

	@Test
	@DisplayName("승인된 가게 상세 조회 실패 - 가게 없음")
	void getApproveStoreDetail_storeNotFound() {
		// given
		UUID storeId = UUID.randomUUID();
		given(storeRepository.findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(storeId, StoreAcceptStatus.APPROVE))
			.willReturn(Optional.empty());

		// when
		GeneralException ex = catchThrowableOfType(
			() -> customerStoreService.getApproveStoreDetail(storeId),
			GeneralException.class
		);

		// then
		assertThat(ex.getErrorReasonHttpStatus().getCode()).isEqualTo("STORE004");
	}

}