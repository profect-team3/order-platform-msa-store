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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import app.domain.store.controller.CustomerStoreController;
import app.domain.store.model.dto.response.GetCustomerStoreDetailResponse;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.service.CustomerStoreService;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;


@WebMvcTest(CustomerStoreController.class)
class CustomerStoreControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CustomerStoreService customerStoreService;

	@Autowired
	private WebApplicationContext context;

	private final UUID storeId = UUID.randomUUID();

	@Test
	@DisplayName("가게 목록 조회 성공")
	@WithMockUser(username = "1", roles = "CUSTOMER")
	void getApprovedStoreList() throws Exception {
		List<GetStoreListResponse> stores = List.of(
			GetStoreListResponse.builder()
				.storeId(storeId)
				.storeName("맛집1")
				.address("서울 강남구")
				.minOrderAmount(3000)
				.averageRating(4.5)
				.build(),

			GetStoreListResponse.builder()
				.storeId(UUID.randomUUID())
				.storeName("맛집2")
				.address("부산 해운대구")
				.minOrderAmount(2000)
				.averageRating(3.9)
				.build()
		);

		Page<GetStoreListResponse> page = new PageImpl<>(stores, PageRequest.of(0, 20), stores.size());
		given(customerStoreService.getApprovedStore(any())).willReturn(PagedResponse.from(page));

		mockMvc.perform(get("/store/customer").with(csrf())
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content.length()").value(2))
			.andExpect(jsonPath("$.result.content[0].storeName").value("맛집1"))
			.andExpect(jsonPath("$.result.content[0].address").value("서울 강남구"))
			.andExpect(jsonPath("$.result.content[0].averageRating").value(4.5));
	}

	@Test
	@DisplayName("가게 상세 조회 성공")
	@WithMockUser(username = "1", roles = "CUSTOMER")
	void getApprovedStoreDetail() throws Exception {
		GetCustomerStoreDetailResponse response = GetCustomerStoreDetailResponse.builder()
			.storeId(storeId)
			.storeName("맛집1")
			.description("한식 맛집")
			.address("서울 강남")
			.phoneNumber("010-1234-5678")
			.minOrderAmount(3000L)
			.categoryName("서울")
			.averageRating(4.5)
			.build();
		given(customerStoreService.getApproveStoreDetail(storeId)).willReturn(response);

		mockMvc.perform(get("/store/customer/{storeId}", storeId).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.storeName").value("맛집1"))
			.andExpect(jsonPath("$.result.phoneNumber").value("010-1234-5678"));
	}

	@Test
	@DisplayName("가게 검색 성공")
	@WithMockUser(username = "1", roles = "CUSTOMER")
	void searchApprovedStore() throws Exception {
		String keyword = "치킨";
		List<GetStoreListResponse> stores = List.of(
			GetStoreListResponse.builder()
				.storeId(storeId)
				.storeName("치킨집")
				.address("서울 강남구")
				.minOrderAmount(3000)
				.averageRating(4.5)
				.build()
		);
		Page<GetStoreListResponse> page = new PageImpl<>(stores, PageRequest.of(0, 10), 1);
		given(customerStoreService.searchStoresByStatus(eq(keyword), any(),any(),any())).willReturn(PagedResponse.from(page));

		mockMvc.perform(get("/store/customer/search").with(csrf())
				.param("keyword", keyword)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content.length()").value(1))
			.andExpect(jsonPath("$.result.content[0].storeName").value("치킨집"));
	}

	@Test
	@DisplayName("가게 검색 결과 없음 - 빈 리스트 반환")
	@WithMockUser(username = "1", roles = "CUSTOMER")
	void searchApprovedStore_emptyResult() throws Exception {
		// given
		Page<GetStoreListResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
		given(customerStoreService.searchStoresByStatus(eq("없는키워드"), any(), any(), any()))
			.willReturn(PagedResponse.from(emptyPage));

		// when & then
		mockMvc.perform(get("/store/customer/search").with(csrf())
				.param("keyword", "없는키워드")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content").isEmpty())
			.andExpect(jsonPath("$.result.totalElements").value(0));
	}

	@Test
	@DisplayName("가게 상세 조회 실패 - 존재하지 않는 가게")
	@WithMockUser(username = "1", roles = "CUSTOMER")
	void getStoreDetail_storeNotFound() throws Exception {
		// given
		given(customerStoreService.getApproveStoreDetail(storeId))
			.willThrow(new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/store/customer/{storeId}", storeId).with(csrf()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value(ErrorStatus.STORE_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.STORE_NOT_FOUND.getMessage()));

	}
}