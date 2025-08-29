package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.commonSecurity.TokenPrincipalParser;
import app.domain.menu.controller.CustomerMenuController;
import app.domain.menu.controller.StoreMenuController;
import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.request.MenuVisibleRequest;
import app.domain.menu.model.dto.response.GetMenuListResponse;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.service.CustomerMenuService;
import app.domain.menu.service.StoreMenuService;
import app.global.apiPayload.PagedResponse;

@WebMvcTest({CustomerMenuController.class, StoreMenuController.class})
@DisplayName("Menu Controllers Test")
public class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerMenuService customerMenuService;

    @MockitoBean
    private StoreMenuService storeMenuService;

    @MockitoBean
    private TokenPrincipalParser tokenPrincipalParser;

    private final UUID TEST_STORE_ID = UUID.randomUUID();
    private final UUID TEST_MENU_ID = UUID.randomUUID();
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 인증된 사용자의 ID가 1L이라고 가정
        when(tokenPrincipalParser.getUserId(any())).thenReturn(String.valueOf(USER_ID));
    }
    @Nested
    @DisplayName("CustomerMenuController Tests")
    class CustomerMenuTests {

        @Test
        @DisplayName("GET /menu/{storeId} - 고객 메뉴 조회 성공")
        @WithMockUser(roles = "CUSTOMER")
        void getMenus_Success() throws Exception {
            // given
            GetMenuListResponse menuDto = GetMenuListResponse.builder().menuId(TEST_MENU_ID).name("테스트 메뉴").build();
            PagedResponse<GetMenuListResponse> mockResponse = new PagedResponse<>(Collections.singletonList(menuDto), 1, 1, 1, 1, true);
            when(customerMenuService.getMenusByStoreId(any(UUID.class), any(Pageable.class))).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/store/menu/{storeId}", TEST_STORE_ID)
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.content[0].name").value("테스트 메뉴"));

            verify(customerMenuService).getMenusByStoreId(eq(TEST_STORE_ID), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("StoreMenuController Tests")
    class StoreMenuTests {

        @Test
        @DisplayName("POST /owner/menu - 점주 메뉴 생성 성공")
        @WithMockUser(roles = "OWNER")
        void createMenu_Success() throws Exception {
            // given
            MenuCreateRequest request = new MenuCreateRequest(TEST_STORE_ID, "신메뉴", 15000L, "맛있는 신메뉴");
            MenuCreateResponse mockResponse = MenuCreateResponse.builder().menuId(TEST_MENU_ID).name("신메뉴").build();
            when(storeMenuService.createMenu(any(MenuCreateRequest.class), anyLong())).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/store/owner/menu")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.name").value("신메뉴"));

            verify(storeMenuService).createMenu(any(MenuCreateRequest.class), eq(USER_ID));
        }

        @Test
        @DisplayName("PUT /owner/menu - 점주 메뉴 수정 성공")
        @WithMockUser(roles = "OWNER")
        void updateMenu_Success() throws Exception {
            // given
            MenuUpdateRequest request = new MenuUpdateRequest(TEST_MENU_ID, "수정된 메뉴", 16000L, "더 맛있는 메뉴", false);
            MenuUpdateResponse mockResponse = MenuUpdateResponse.builder().menuId(TEST_MENU_ID).name("수정된 메뉴").build();
            when(storeMenuService.updateMenu(any(MenuUpdateRequest.class), anyLong())).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(put("/store/owner/menu")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.name").value("수정된 메뉴"));

            verify(storeMenuService).updateMenu(any(MenuUpdateRequest.class), eq(USER_ID));
        }

        @Test
        @DisplayName("DELETE /owner/menu/delete - 점주 메뉴 삭제 성공")
        @WithMockUser(roles = "OWNER")
        void deleteMenu_Success() throws Exception {
            // given
            MenuDeleteRequest request = new MenuDeleteRequest(TEST_MENU_ID);
            MenuDeleteResponse mockResponse = new MenuDeleteResponse(TEST_MENU_ID, "DELETED");
            when(storeMenuService.deleteMenu(any(), anyLong())).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(delete("/store/owner/menu/delete")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.status").value("DELETED"));

            verify(storeMenuService).deleteMenu(any(MenuDeleteRequest.class), eq(USER_ID));
        }

        @Test
        @DisplayName("PUT /owner/menu/visible - 점주 메뉴 노출상태 변경 성공")
        @WithMockUser(roles = "OWNER")
        void updateMenuVisibility_Success() throws Exception {
            // given
            MenuVisibleRequest request = new MenuVisibleRequest(TEST_MENU_ID, true);
            MenuUpdateResponse mockResponse = MenuUpdateResponse.builder().menuId(TEST_MENU_ID).name("테스트 메뉴").build();
            when(storeMenuService.updateMenuVisibility(any(UUID.class), anyBoolean(), anyLong())).thenReturn(mockResponse);

            // when & then
            mockMvc.perform(put("/store/owner/menu/visible")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.menuId").value(TEST_MENU_ID.toString()));

            verify(storeMenuService).updateMenuVisibility(eq(TEST_MENU_ID), eq(true), eq(USER_ID));
        }
    }
}
