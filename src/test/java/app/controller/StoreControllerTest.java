package app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.exception.ExceptionAdvice; // ExceptionAdvice import 추가
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper(); // 수동 생성

    @Mock
    private StoreService storeService;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private StoreController storeController;

    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
            .setControllerAdvice(new ExceptionAdvice()) // ExceptionAdvice 등록
            .build();
    }

    @Test
    @DisplayName("가게 생성 API 성공")
    void createStoreApi_Success() throws Exception {
        StoreApproveRequest request = new StoreApproveRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Test Address",
            "Test Store",
            "Test Description",
            "010-1234-5678",
            10000L
        );

        StoreApproveResponse mockResponse = new StoreApproveResponse(UUID.randomUUID(), StoreAcceptStatus.PENDING.name());

        when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.of(mock(Region.class)));
        when(storeRepository.existsByStoreNameAndRegion(anyString(), any(Region.class))).thenReturn(false);
        when(storeService.createStore(any(StoreApproveRequest.class), eq(testUserId))).thenReturn(mockResponse);

        mockMvc.perform(post("/store")
                .header("X-User-ID", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.code").value("STORE201"))
            .andExpect(jsonPath("$.message").value("가게가 성공적으로 생성되었습니다."))
            .andExpect(jsonPath("$.result.storeApprovalStatus").value(StoreAcceptStatus.PENDING.name()));

        verify(storeService).createStore(any(StoreApproveRequest.class), eq(testUserId));
    }

    @Test
    @DisplayName("가게 생성 API 실패 - 유효하지 않은 요청")
    void createStoreApi_InvalidRequest() throws Exception {
        StoreApproveRequest invalidRequest = new StoreApproveRequest(
            null,
            null,
            null,
            "Invalid Store",
            null,
            null,
            null
        );

        // When & Then
        mockMvc.perform(post("/store")
                .header("X-User-ID", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.code").value(StoreErrorCode.REGION_ID_NULL.getCode()));

        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("가게 생성 API 실패 - 중복된 가게 이름")
    void createStoreApi_DuplicateStoreName() throws Exception {
        StoreApproveRequest request = new StoreApproveRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Test Address",
            "Duplicate Store",
            "Test Description",
            "010-1234-5678",
            10000L
        );

        Region mockRegion = mock(Region.class);
        when(regionRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockRegion));
        when(storeRepository.existsByStoreNameAndRegion(anyString(), any(Region.class))).thenReturn(true); // 중복 발생

        mockMvc.perform(post("/store")
                .header("X-User-ID", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.code").value(StoreErrorCode.DUPLICATE_STORE_NAME_IN_REGION.getCode()));

        verifyNoInteractions(storeService);
    }

}