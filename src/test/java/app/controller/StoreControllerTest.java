package app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private StoreService storeService;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private StoreController storeController;

    private final Long testUserId = 1L;
    private final UUID TEST_REGION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("가게 생성 API 테스트")
    class CreateStoreApiTest {

        @Test
        @DisplayName("성공: 가게 생성")
        void createStoreSuccess() {
            StoreApproveRequest request = new StoreApproveRequest(
                TEST_REGION_ID,
                UUID.randomUUID(),
                "Test Address",
                "Test Store",
                "Test Description",
                "010-1234-5678",
                10000L
            );

            StoreApproveResponse expectedResponse = new StoreApproveResponse(UUID.randomUUID(), StoreAcceptStatus.PENDING.name());

            when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.of(mock(Region.class)));
            when(storeRepository.existsByStoreNameAndRegion(anyString(), any(Region.class))).thenReturn(false);
            when(storeService.createStore(any(StoreApproveRequest.class), eq(testUserId))).thenReturn(expectedResponse);

            ApiResponse<StoreApproveResponse> response = storeController.createStore(request, testUserId);

            assertTrue(response.isSuccess());
            assertEquals("STORE201", response.code());
            assertEquals("가게가 성공적으로 생성되었습니다.", response.message());
            assertEquals(StoreAcceptStatus.PENDING.name(), response.result().getStoreApprovalStatus());
            verify(storeService, times(1)).createStore(any(StoreApproveRequest.class), eq(testUserId));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 요청 (예: 지역 ID 누락)")
        void createStoreFailInvalidRequest() {
            StoreApproveRequest invalidRequest = new StoreApproveRequest(
                null,
                UUID.randomUUID(),
                "Test Address",
                "Invalid Store",
                "Test Description",
                "010-1234-5678",
                10000L
            );

            GeneralException exception = assertThrows(GeneralException.class, () -> {
                storeController.createStore(invalidRequest, testUserId);
            });

            assertEquals(StoreErrorCode.REGION_ID_NULL, exception.getCode());
            verifyNoInteractions(storeService);
        }

        @Test
        @DisplayName("실패: 이미 존재하는 가게 이름")
        void createStoreFailStoreNameAlreadyExists() {
            StoreApproveRequest request = new StoreApproveRequest(
                TEST_REGION_ID,
                UUID.randomUUID(),
                "Test Address",
                "Existing Store",
                "Test Description",
                "010-1234-5678",
                10000L
            );

            when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.of(mock(Region.class)));
            when(storeRepository.existsByStoreNameAndRegion(anyString(), any(Region.class))).thenReturn(true);

            GeneralException exception = assertThrows(GeneralException.class, () -> {
                storeController.createStore(request, testUserId);
            });

            assertEquals(StoreErrorCode.DUPLICATE_STORE_NAME_IN_REGION, exception.getCode());
            verifyNoInteractions(storeService);
        }
    }
}