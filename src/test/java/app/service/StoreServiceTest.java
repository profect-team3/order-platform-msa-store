package app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.store.service.StoreService;
import app.domain.store.client.OrderClient;
import app.domain.store.client.ReviewClient;
import app.domain.store.client.UserClient;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.*;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;
import app.domain.store.status.StoreErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService Test")
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderClient orderClient;
    @Mock
    private ReviewClient reviewClient;
    @Mock
    private UserClient userClient;

    private Long testUserId;
    private Long otherUserId;
    private UUID testStoreId;
    private UUID testRegionId;
    private UUID testCategoryId;
    private StoreApproveRequest storeApproveRequest;
    private Region mockRegion;
    private Category mockCategory;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        otherUserId = 2L;
        testStoreId = UUID.randomUUID();
        testRegionId = UUID.randomUUID();
        testCategoryId = UUID.randomUUID();

        storeApproveRequest = new StoreApproveRequest(
            testRegionId,
            testCategoryId,
            "Test Address",
            "Test Store",
            "Test Description",
            "010-1234-5678",
            10000L
        );
		Category parent=new Category(testCategoryId,"Parent",null);
        mockRegion = new Region(testRegionId, "REGION_CODE", "Region Name", true, "Full Region Name", "Sido", "Sigungu", "Eupmyendong");
        mockCategory = new Category(testCategoryId, "Category Name",parent);
        mockStore = new Store(testStoreId, testUserId, mockRegion, mockCategory, "Test Store", "Test Description", "Test Address", "010-1234-5678", 10000L, StoreAcceptStatus.PENDING);
    }

    // --- createStore Tests ---

    @Test
    @DisplayName("가게 생성 성공")
    void createStore_Success() {
        // given
        when(userClient.isUserExists()).thenReturn(new ApiResponse<>(true, "200", "OK", true));
        when(regionRepository.findById(testRegionId)).thenReturn(Optional.of(mockRegion));
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(mockCategory));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        // when
        StoreApproveResponse response = storeService.createStore(storeApproveRequest, testUserId);

        // then
        assertNotNull(response);
        assertEquals(mockStore.getStoreId(), response.getStoreId());
        assertEquals(StoreAcceptStatus.PENDING.name(), response.getStoreApprovalStatus());

        verify(userClient).isUserExists();
        verify(regionRepository).findById(testRegionId);
        verify(categoryRepository).findById(testCategoryId);
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    @DisplayName("가게 생성 실패 - 사용자 없음")
    void createStore_UserNotFound() {
        // given
        when(userClient.isUserExists()).thenReturn(new ApiResponse<>(true, "200", "OK", false));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeService.createStore(storeApproveRequest, testUserId));

        assertEquals(StoreErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorReason().getCode());
        verify(userClient).isUserExists();
        verifyNoInteractions(regionRepository, categoryRepository, storeRepository);
    }
    
    // ... other createStore tests ...

    // --- updateStoreInfo Tests ---

    @Test
    @DisplayName("가게 정보 수정 성공")
    void updateStoreInfo_Success() {
        // given
        StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(testStoreId, null, "Updated Name", "Updated Address", null, null, null);
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        // when
        StoreInfoUpdateResponse response = storeService.updateStoreInfo(request, testUserId);

        // then
        assertNotNull(response);
        assertEquals(testStoreId, response.getStoreId());
        verify(storeRepository).findById(testStoreId);
        verify(storeRepository).save(any(Store.class));
        assertEquals("Updated Name", mockStore.getStoreName());
        assertEquals("Updated Address", mockStore.getAddress());
    }

    // ... other updateStoreInfo tests ...

    // --- deleteStore Tests ---

    @Test
    @DisplayName("가게 삭제 성공")
    void deleteStore_Success() {
        // given
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));

        // when
        assertDoesNotThrow(() -> storeService.deleteStore(testStoreId, testUserId));

        // then
        verify(storeRepository).findById(testStoreId);
        assertNotNull(mockStore.getDeletedAt());
    }

    // ... other deleteStore tests ...

    // --- getStoreMenuList Tests ---
    @Test
    @DisplayName("가게 메뉴 목록 조회 성공")
    void getStoreMenuList_Success() {
        // given
        Menu menu1 = Menu.builder().name("Menu 1").price(10000L).build();
        Menu menu2 = Menu.builder().name("Menu 2").price(15000L).build();
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(menuRepository.findByStoreAndDeletedAtIsNull(mockStore)).thenReturn(List.of(menu1, menu2));

        // when
        MenuListResponse response = storeService.getStoreMenuList(testStoreId, testUserId);

        // then
        assertNotNull(response);
        assertEquals(testStoreId, response.getStoreId());
        assertEquals(2, response.getMenus().size());
        verify(storeRepository).findById(testStoreId);
        verify(menuRepository).findByStoreAndDeletedAtIsNull(mockStore);
    }

    // --- getStoreReviewList Tests ---
    @Test
    @DisplayName("가게 리뷰 목록 조회 성공")
    void getStoreReviewList_Success() {
        // given
		GetReviewResponse review1 = mock(GetReviewResponse.class);
        GetReviewResponse review2= mock(GetReviewResponse.class);
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(reviewClient.getReviewsByStoreId(testStoreId)).thenReturn(new ApiResponse<>(true, "200", "OK", List.of(review1,review2)));

        // when
        List<GetReviewResponse> response = storeService.getStoreReviewList(testStoreId, testUserId);

        // then
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(storeRepository).findById(testStoreId);
        verify(reviewClient).getReviewsByStoreId(testStoreId);
    }

    // --- getStoreOrderList Tests ---
    @Test
    @DisplayName("가게 주문 목록 조회 성공")
    void getStoreOrderList_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        StoreOrderInfo orderInfo = new StoreOrderInfo(orderId, UUID.randomUUID(), testUserId,25000L, "PENDING", LocalDateTime.now());
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(orderClient.getOrdersByStoreId(testStoreId)).thenReturn(new ApiResponse<>(true, "200", "OK", List.of(orderInfo)));
        when(userClient.getUserName()).thenReturn(new ApiResponse<>(true, "200", "OK", "Customer Name"));

        // when
        StoreOrderListResponse response = storeService.getStoreOrderList(testStoreId, testUserId);

        // then
        assertNotNull(response);
        assertEquals(1, response.getOrderList().size());
        assertEquals(orderId, response.getOrderList().get(0).getOrderId());
        assertEquals("Customer Name", response.getOrderList().get(0).getCustomerName());
        verify(storeRepository).findById(testStoreId);
        verify(orderClient).getOrdersByStoreId(testStoreId);
        verify(userClient).getUserName();
    }

    // --- acceptOrder Tests ---
    @Test
    @DisplayName("주문 승인 성공")
    void acceptOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderInfo orderInfo = new OrderInfo(orderId, testStoreId, 2L, 25000L, "PENDING", LocalDateTime.now());
        when(orderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "OK", orderInfo));
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(orderClient.updateOrderStatus(orderId, "ACCEPTED")).thenReturn(new ApiResponse<>(true, "200", "OK", "Status updated"));

        // when
        assertDoesNotThrow(() -> storeService.acceptOrder(orderId, testUserId));

        // then
        verify(orderClient).getOrderInfo(orderId);
        verify(storeRepository).findById(testStoreId);
        verify(orderClient).updateOrderStatus(orderId, "ACCEPTED");
    }

    @Test
    @DisplayName("주문 승인 실패 - 가게 소유주 아님")
    void acceptOrder_NotStoreOwner() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderInfo orderInfo = new OrderInfo(orderId, testStoreId, 2L, 25000L, "PENDING", LocalDateTime.now());
        when(orderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "OK", orderInfo));
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeService.acceptOrder(orderId, otherUserId));

        assertEquals(StoreErrorCode.NOT_STORE_OWNER.getCode(), exception.getErrorReason().getCode());
    }

    // --- rejectOrder Tests ---
    @Test
    @DisplayName("주문 거절 성공")
    void rejectOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderInfo orderInfo = new OrderInfo(orderId, testStoreId, 2L, 25000L, "PENDING", LocalDateTime.now());
        when(orderClient.getOrderInfo(orderId)).thenReturn(new ApiResponse<>(true, "200", "OK", orderInfo));
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(orderClient.updateOrderStatus(orderId, "REJECTED")).thenReturn(new ApiResponse<>(true, "200", "OK", "Status updated"));

        // when
        assertDoesNotThrow(() -> storeService.rejectOrder(orderId, testUserId));

        // then
        verify(orderClient).getOrderInfo(orderId);
        verify(storeRepository).findById(testStoreId);
        verify(orderClient).updateOrderStatus(orderId, "REJECTED");
    }
}
