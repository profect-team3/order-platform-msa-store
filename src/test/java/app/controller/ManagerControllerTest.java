package app.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.commonUtil.apiPayload.PagedResponse;
import app.domain.store.controller.ManagerController;
import app.domain.store.model.dto.response.GetStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.service.ManagerService;
import app.domain.store.status.StoreAcceptStatus;

@WebMvcTest(ManagerController.class)
@DisplayName("ManagerController Test")
class ManagerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ManagerService managerService;

	@Test
	@DisplayName("가게 리스트 조회 - 승인 상태로 필터링")
	@WithMockUser(username = "1", roles = "MANAGER")
	void testGetAllStoreWithStatus() throws Exception {
		// given
		PagedResponse<GetStoreListResponse> mockResponse =
			new PagedResponse<>(List.of(), 0, 0, 0, 0, true);

		when(managerService.getAllStore(eq(StoreAcceptStatus.PENDING), any(Pageable.class)))
			.thenReturn(mockResponse);

		// when & then
		mockMvc.perform(get("/manager/store")
				.with(csrf())
				.param("status", "PENDING")
				.param("page", "0")
				.param("size", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").exists());
	}

	@Test
	@DisplayName("가게 상세 조회")
	@WithMockUser(username = "1", roles = "MANAGER")
	void testGetStoreById() throws Exception {
		// given
		UUID storeId = UUID.randomUUID();

		GetStoreDetailResponse response = GetStoreDetailResponse.builder()
			.storeId(storeId)
			.storeName("감자탕 명가")
			.description("진한 국물의 감자탕")
			.address("서울시 종로구 종로1가")
			.phoneNumber("010-2222-3333")
			.minOrderAmount(15000L)
			.regionName("종로구")
			.categoryName("한식")
			.averageRating(4.3)
			.ownerId(2L)
			.ownerEmail("adfjf@naver")
			.ownerName("akdfj1234")
			.ownerRealName("김길동")
			.ownerPhone("010123434")
			.build();
		when(managerService.getStoreDetail(storeId)).thenReturn(response);

		// when & then
		mockMvc.perform(get("/manager/store/{storeId}", storeId).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").exists());
	}

	@Test
	@DisplayName("가게 승인 처리")
	@WithMockUser(username = "1", roles = "MANAGER")
	void testApproveStore() throws Exception {
		// given
		UUID storeId = UUID.randomUUID();
		StoreAcceptStatus status = StoreAcceptStatus.APPROVE;
		String message = "가게 상태가 APPROVED 처리되었습니다.";

		when(managerService.approveStore(storeId, status)).thenReturn(message);

		// when & then
		mockMvc.perform(patch("/manager/store/{storeId}/accept", storeId).with(csrf())
				.param("status", status.name()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value(message));
	}

	@Test
	@DisplayName("가게 검색 - 키워드, 카테고리, 상태로 검색")
	@WithMockUser(roles = "MANAGER")
	void testSearchStore() throws Exception {
		// given
		String keyword = "치킨";
		String category = "한식";
		StoreAcceptStatus status = StoreAcceptStatus.APPROVE;

		GetStoreListResponse storeDto = GetStoreListResponse.builder()
			.storeId(UUID.randomUUID())
			.storeName("대박치킨")
			.build();

		PagedResponse<GetStoreListResponse> mockResponse = new PagedResponse<>(List.of(storeDto), 1, 1, 1, 1, true);

		when(managerService.searchStore(eq(keyword), eq(category), eq(status), any(Pageable.class)))
			.thenReturn(mockResponse);

		// when & then
		mockMvc.perform(get("/manager/store/search")
				.param("keyword", keyword)
				.param("category", category)
				.param("status", status.name()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.result.content[0].storeName").value("대박치킨"));

		verify(managerService).searchStore(eq(keyword), eq(category), eq(status), any(Pageable.class));
	}
}