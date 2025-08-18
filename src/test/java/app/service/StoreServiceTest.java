package app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import app.domain.menu.model.entity.Category;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.store.StoreService;
import app.domain.store.client.OrderClient;
import app.domain.store.client.ReviewClient;
import app.domain.store.client.UserClient;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.exception.GeneralException;
import app.domain.store.status.StoreErrorCode;

@ExtendWith(MockitoExtension.class)
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
    private UUID testRegionId;
    private UUID testCategoryId;
    private StoreApproveRequest storeApproveRequest;
    private Region mockRegion;
    private Category mockCategory;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
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

        mockRegion = new Region(testRegionId, "REGION_CODE", "Region Name", true, "Full Region Name", "Sido", "Sigungu", "Eupmyendong");
        mockCategory = new Category(testCategoryId, "Category Name");
        mockStore = new Store(UUID.randomUUID(), testUserId, mockRegion, mockCategory, "Test Store", "Test Description", "Test Address", "010-1234-5678", 10000L, StoreAcceptStatus.PENDING);
    }

    @Test
    @DisplayName("가게 생성 성공")
    void createStore_Success() {
        when(userClient.isUserExists()).thenReturn(true);
        when(regionRepository.findById(testRegionId)).thenReturn(Optional.of(mockRegion));
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(mockCategory));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        StoreApproveResponse response = storeService.createStore(storeApproveRequest);

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
        when(userClient.isUserExists(testUserId)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeService.createStore(storeApproveRequest, testUserId));

        assertEquals(StoreErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorReason().getCode());
        verify(userClient).isUserExists(testUserId);
        verifyNoInteractions(regionRepository, categoryRepository, storeRepository);
    }

}
