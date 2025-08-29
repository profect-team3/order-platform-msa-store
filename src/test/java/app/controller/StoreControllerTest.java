package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.commonUtil.apiPayload.exception.GeneralException;
import app.commonUtil.security.TokenPrincipalParser;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.store.controller.StoreController;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.GetReviewResponse;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.dto.response.StoreOrderListResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.service.StoreService;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;

@WebMvcTest(StoreController.class)
@DisplayName("StoreController 테스트")
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

	@MockitoBean
    private TokenPrincipalParser tokenPrincipalParser;

	@MockitoBean
    private StoreRepository storeRepository;

	@MockitoBean
    private RegionRepository regionRepository;

    private final UUID TEST_STORE_ID = UUID.randomUUID();
    private final UUID TEST_REGION_ID = UUID.randomUUID();
    private final UUID TEST_CATEGORY_ID = UUID.randomUUID();
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 인증된 사용자의 ID가 1L이라고 가정
        when(tokenPrincipalParser.getUserId(any())).thenReturn(String.valueOf(USER_ID));
    }

    @Nested
    @DisplayName("가게 등록 API [POST /owner] 테스트")
    class CreateStoreTest {

        @Test
        @DisplayName("성공: 가게 등록 요청")
        @WithMockUser(roles = "OWNER")
        void createStoreSuccess() throws Exception {
			Region region = new Region(UUID.randomUUID(),
				       "regionCode","regionName",true,"fullName","sido","sigungu",
				       "eupmyendong");
			// given
            StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
                "테스트 설명", "01012345678", 1000L);
            StoreApproveResponse expectedResponse = new StoreApproveResponse(TEST_STORE_ID, "PENDING");

            // 컨트롤러의 validateCreateStoreRequest가 통과되도록 repository mock 설정
            when(regionRepository.findById(any())).thenReturn(Optional.of(region));
            when(storeRepository.existsByStoreNameAndRegion(any(), any())).thenReturn(false);
            when(storeService.createStore(any(StoreApproveRequest.class), anyLong())).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(post("/store/owner")
                    .with(csrf()) // CSRF 토큰 추가
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_CREATED_SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.storeId").value(expectedResponse.getStoreId().toString()));

            verify(storeService).createStore(any(StoreApproveRequest.class), eq(USER_ID));
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 API [PUT /owner] 테스트")
    class UpdateStoreTest {

        @Test
        @DisplayName("성공: 가게 정보 수정")
        @WithMockUser(roles = "OWNER")
        void updateStoreSuccess() throws Exception {
            // given
            StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(TEST_STORE_ID, TEST_CATEGORY_ID, "새 가게 이름",
                "새 주소", "01098765432", 2000L, "새 설명");
            StoreInfoUpdateResponse expectedResponse = new StoreInfoUpdateResponse(TEST_STORE_ID);
            when(storeService.updateStoreInfo(any(StoreInfoUpdateRequest.class), anyLong())).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(put("/store/owner")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_UPDATED_SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.storeId").value(expectedResponse.getStoreId().toString()));

            verify(storeService).updateStoreInfo(any(StoreInfoUpdateRequest.class), eq(USER_ID));
        }
    }

    @Nested
    @DisplayName("가게 삭제 API [DELETE /owner/{storeId}] 테스트")
    class DeleteStoreTest {

        @Test
        @DisplayName("성공: 가게 삭제")
        @WithMockUser(roles = "OWNER")
        void deleteStoreSuccess() throws Exception {
            // given
            doNothing().when(storeService).deleteStore(any(UUID.class), anyLong());

            // when & then
            mockMvc.perform(delete("/store/owner/{storeId}", TEST_STORE_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getCode()));

            verify(storeService).deleteStore(eq(TEST_STORE_ID), eq(USER_ID));
        }
    }

    @Nested
    @DisplayName("점주 GET API [/owner/{storeId}/...] 테스트")
    class OwnerGetApisTest {

        @Test
        @DisplayName("성공: 메뉴 목록 조회")
        @WithMockUser(roles = "OWNER")
        void getStoreMenus_Success() throws Exception {
            // given
            MenuListResponse.MenuDetail menuDetail = new MenuListResponse.MenuDetail(UUID.randomUUID(), "메뉴1", 1000L, "설명1", false);
            MenuListResponse expectedResponse = new MenuListResponse(TEST_STORE_ID, Collections.singletonList(menuDetail));
            when(storeService.getStoreMenuList(any(UUID.class), anyLong())).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(get("/store/owner/{storeId}/menu", TEST_STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.storeId").value(TEST_STORE_ID.toString()));

            verify(storeService).getStoreMenuList(eq(TEST_STORE_ID), eq(USER_ID));
        }

        @Test
        @DisplayName("성공: 리뷰 목록 조회")
        @WithMockUser(roles = "OWNER")
        void getStoreReviews_Success() throws Exception {
            // given
			GetReviewResponse reviewResponse = new
        GetReviewResponse(UUID.randomUUID(),"username","storeName", 5, "맛있어요", null);
            List<GetReviewResponse> expectedResponse = Collections.singletonList(reviewResponse);
            when(storeService.getStoreReviewList(any(UUID.class), anyLong())).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(get("/store/owner/{storeId}/review", TEST_STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].content").value("맛있어요"));

            verify(storeService).getStoreReviewList(eq(TEST_STORE_ID), eq(USER_ID));
        }

        @Test
        @DisplayName("성공: 주문 목록 조회")
        @WithMockUser(roles = "OWNER")
        void getStoreOrders_Success() throws Exception {
            // given
			StoreOrderListResponse.StoreOrderDetail orderDetail = new StoreOrderListResponse.StoreOrderDetail(UUID.randomUUID(), USER_ID, "고객1", 15000L,
				       "COMPLETED", LocalDateTime.now());
			StoreOrderListResponse expectedResponse = new StoreOrderListResponse(TEST_STORE_ID, Collections.singletonList(orderDetail));
            when(storeService.getStoreOrderList(any(UUID.class), anyLong())).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(get("/store/owner/{storeId}/order", TEST_STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.storeId").value(TEST_STORE_ID.toString()));

            verify(storeService).getStoreOrderList(eq(TEST_STORE_ID), eq(USER_ID));
        }
    }

    @Nested
    @DisplayName("주문 수락/거절 API [POST /owner/order/{orderId}/...] 테스트")
    class OrderAcceptRejectTest {

        @Test
        @DisplayName("성공: 주문 수락")
        @WithMockUser(roles = "OWNER")
        void acceptOrder_success() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            doNothing().when(storeService).acceptOrder(any(UUID.class), anyLong());

            // when & then
            mockMvc.perform(post("/store/owner/order/{orderId}/accept", orderId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StoreSuccessStatus.ORDER_ACCEPTED_SUCCESS.getCode()));

            verify(storeService).acceptOrder(eq(orderId), eq(USER_ID));
        }

        @Test
        @DisplayName("성공: 주문 거절")
        @WithMockUser(roles = "OWNER")
        void rejectOrder_success() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            doNothing().when(storeService).rejectOrder(any(UUID.class), anyLong());

            // when & then
            mockMvc.perform(post("/store/owner/order/{orderId}/reject", orderId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StoreSuccessStatus.ORDER_REJECTED_SUCCESS.getCode()));

            verify(storeService).rejectOrder(eq(orderId), eq(USER_ID));
        }

        @Test
        @DisplayName("실패: 주문 수락 시 가게 없음 (404 Not Found)")
        @WithMockUser(roles = "OWNER")
        void acceptOrder_fail_storeNotFound() throws Exception {
            // given
            UUID orderId = UUID.randomUUID();
            doThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND)).when(storeService).acceptOrder(any(UUID.class), anyLong());

            // when & then
            mockMvc.perform(post("/store/owner/order/{orderId}/accept", orderId).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(StoreErrorCode.STORE_NOT_FOUND.getCode()));

            verify(storeService).acceptOrder(eq(orderId), eq(USER_ID));
        }
    }
}
