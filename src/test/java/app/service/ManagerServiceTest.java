package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

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

import app.domain.menu.model.entity.Category;
import app.domain.store.client.ReviewClient;
import app.domain.store.client.UserClient;
import app.domain.store.model.StoreQueryRepository;
import app.domain.store.model.dto.response.GetStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.dto.response.GetUserInfoResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.service.ManagerService;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.SuccessStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerService Test")
public class ManagerServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private StoreQueryRepository storeQueryRepository;

	@Mock
	private ReviewClient reviewClient;

	@Mock
	private UserClient userClient;

	@InjectMocks
	private ManagerService managerService;

	@Test
	@DisplayName("가게 상세 조회 성공")
	void getStoreDetail_success() {
		// given
		UUID storeId = UUID.randomUUID();
		Long ownerId = 1L;

		Region region = Region.builder()
			.regionName("마포구")
			.build();
		List<UUID> storeIds=List.of(storeId);

		Category category = Category.builder()
			.categoryName("한식")
			.build();

		ReviewClient.StoreReviewResponse reviewResponse=mock(ReviewClient.StoreReviewResponse.class);

		GetUserInfoResponse userResponse =mock(GetUserInfoResponse.class);

		Store store = Store.builder()
			.storeId(storeId)
			.storeName("맛있는 족발집")
			.description("국내산 족발 사용")
			.address("서울시 마포구")
			.phoneNumber("010-1234-5678")
			.minOrderAmount(15000L)
			.storeAcceptStatus(StoreAcceptStatus.APPROVE)
			.region(region)
			.category(category)
			.userId(ownerId)
			.build();

		when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
		when(reviewClient.getStoreReviewAverage(storeIds)).thenReturn(ApiResponse.onSuccess(SuccessStatus._OK,List.of(reviewResponse)));
		when(userClient.getUserInfo()).thenReturn(ApiResponse.onSuccess(SuccessStatus._OK,userResponse));

		// when
		GetStoreDetailResponse response = managerService.getStoreDetail(storeId);

		// then
		assertThat(response.getStoreId()).isEqualTo(storeId);
		assertThat(response.getStoreName()).isEqualTo("맛있는 족발집");
		assertThat(response.getDescription()).isEqualTo("국내산 족발 사용");
		assertThat(response.getAddress()).isEqualTo("서울시 마포구");
		assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
		assertThat(response.getMinOrderAmount()).isEqualTo(15000L);
		assertThat(response.getRegionName()).isEqualTo("마포구");
		assertThat(response.getCategoryName()).isEqualTo("한식");
	}

	@Test
	@DisplayName("가게 승인 처리 성공")
	void approveStore_success() {
		// given
		UUID storeId = UUID.randomUUID();
		Region region = Region.builder().regionName("마포구").build();
		Category category = Category.builder().categoryName("한식").build();
		Store store = Store.builder()
			.storeId(storeId)
			.storeName("맛있는 족발집")
			.description("국내산 족발 사용")
			.address("서울시 마포구")
			.phoneNumber("010-1234-5678")
			.minOrderAmount(15000L)
			.storeAcceptStatus(StoreAcceptStatus.APPROVE)
			.region(region)
			.category(category)
			.build();
		store.updateAcceptStatus(StoreAcceptStatus.PENDING);

		when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

		// when
		String result = managerService.approveStore(storeId, StoreAcceptStatus.APPROVE);

		// then
		assertThat(result).contains("변경 되었습니다");
		assertThat(store.getStoreAcceptStatus()).isEqualTo(StoreAcceptStatus.APPROVE);
	}

	@Test
	@DisplayName("가게 리스트 조회 성공")
	void getAllStore_success() {
		// given
		UUID storeId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);

		GetStoreListResponse dto = GetStoreListResponse.builder()
			.storeId(storeId)
			.storeName("맛있는 족발집")
			.address("서울시 마포구")
			.minOrderAmount(15000L)
			.averageRating(4.0)
			.build();

		Page<GetStoreListResponse> page = new PageImpl<>(List.of(dto), pageable, 1);

		given(storeQueryRepository.getAllStore(StoreAcceptStatus.APPROVE, pageable))
			.willReturn(PagedResponse.from(page));

		// when
		PagedResponse<GetStoreListResponse> response = managerService.getAllStore(StoreAcceptStatus.APPROVE, pageable);

		// then
		assertThat(response.getContent()).hasSize(1);
		assertThat(response.getContent().get(0).getStoreName()).isEqualTo("맛있는 족발집");
		assertThat(response.getContent().get(0).getAverageRating()).isEqualTo(4.0);
	}

	@Test
	@DisplayName("가게 키워드 검색 성공")
	void searchStore_success() {
		// given
		UUID storeId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		GetStoreListResponse responseDto = GetStoreListResponse.builder()
			.storeId(storeId)
			.storeName("맛있는 족발집")
			.address("서울시 마포구")
			.minOrderAmount(15000L)
			.averageRating(4.5)
			.build();

		Page<GetStoreListResponse> page = new PageImpl<>(List.of(responseDto), pageable, 1);
		PagedResponse<GetStoreListResponse> pagedResponse = PagedResponse.from(page);

		when(storeQueryRepository.searchStores("족발", "keyword",StoreAcceptStatus.PENDING, pageable))
			.thenReturn(pagedResponse);

		// when
		PagedResponse<GetStoreListResponse> response = managerService.searchStore("족발", "keyword",StoreAcceptStatus.PENDING,
			pageable);

		// then
		assertThat(response.getContent()).hasSize(1);
		GetStoreListResponse dto = response.getContent().get(0);
		assertThat(dto.getStoreId()).isEqualTo(storeId);
		assertThat(dto.getStoreName()).isEqualTo("맛있는 족발집");
		assertThat(dto.getAddress()).isEqualTo("서울시 마포구");
		assertThat(dto.getMinOrderAmount()).isEqualTo(15000L);
		assertThat(dto.getAverageRating()).isEqualTo(4.5);
	}

	@Test
	@DisplayName("가게 상세 조회 실패 - 존재하지 않는 storeId")
	void getStoreDetail_notFound_shouldThrowException() {
		// given
		UUID invalidId = UUID.randomUUID();
		when(storeRepository.findByStoreIdAndDeletedAtIsNull(invalidId)).thenReturn(Optional.empty());

		// when
		GeneralException ex = catchThrowableOfType(
			() -> managerService.getStoreDetail(invalidId),
			GeneralException.class
		);

		// then
		assertThat(ex.getErrorReasonHttpStatus().getCode()).isEqualTo("STORE004");
	}

	@Test
	@DisplayName("가게 승인 실패 - 존재하지 않는 storeId")
	void approveStore_notFound_shouldThrowException() {
		// given
		UUID invalidId = UUID.randomUUID();
		when(storeRepository.findByStoreIdAndDeletedAtIsNull(invalidId)).thenReturn(Optional.empty());

		// when
		GeneralException ex = catchThrowableOfType(
			() -> managerService.approveStore(invalidId, StoreAcceptStatus.APPROVE),
			GeneralException.class
		);

		// then
		assertThat(ex.getErrorReasonHttpStatus().getCode()).isEqualTo("STORE004");
	}

	@Test
	@DisplayName("가게 승인 실패 - 이미 같은 상태")
	void approveStore_sameStatus_shouldThrowException() {
		// given
		UUID storeId = UUID.randomUUID();
		Store store = Store.builder()
			.storeId(storeId)
			.storeName("맛있는 족발집")
			.storeAcceptStatus(StoreAcceptStatus.APPROVE)
			.build();

		when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

		// when
		GeneralException ex = catchThrowableOfType(
			() -> managerService.approveStore(storeId, StoreAcceptStatus.APPROVE),
			GeneralException.class
		);

		// then
		assertThat(ex.getErrorReasonHttpStatus().getCode()).isEqualTo("STORE019");
	}
}
