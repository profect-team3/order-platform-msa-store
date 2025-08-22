package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import app.domain.mongo.StoreCollectionQueryController;
import app.domain.mongo.StoreCollectionQueryService;
import app.domain.mongo.model.entity.MenuCollection;
import app.domain.mongo.model.entity.StoreCollection;
import app.domain.mongo.status.MongoStoreMenuSuccessCode;

@WebMvcTest(StoreCollectionQueryController.class)
@DisplayName("StoreCollectionQueryController 테스트")
public class StoreCollectionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreCollectionQueryService storeCollectionQueryService;

    private StoreCollection createTestStore(String storeKey, String storeName) {
        MenuCollection menu = new MenuCollection(UUID.randomUUID().toString(), "Test Menu", 10000, "A delicious test menu", "CHICKEN", false);
        return new StoreCollection(
            UUID.randomUUID().toString(),      // id
            1L,                                // userId
            storeKey,                          // storeKey
            storeName,                         // storeName
            "Test Description",                // description
            List.of("CHICKEN"),                // categoryKeys
            4.5,                               // avgRating
            10L,                               // reviewCount
            "010-1234-5678",                   // phoneNumber
            10000L,                            // minOrderAmount
            "Test Address",                    // address
            "GANGNAM",                         // regionName
            "SEOUL GANGNAM",                   // regionFullName
            "APPROVED",                        // storeAcceptStatus
            true,                              // isActive
            null,                              // createdAt
            null,                              // updatedAt
            null,                              // deletedAt
            1L,                                // version
            List.of(menu)                      // menus
        );
    }

    @Nested
    @DisplayName("통합 검색 API [/mongo/stores/search] 테스트")
    class SearchStoresTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 키워드로 가게 검색")
        void searchStores_Success() throws Exception {
            String keyword = "Test";
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Store");
            when(storeCollectionQueryService.searchStores(anyString())).thenReturn(Collections.singletonList(testStore));

            mockMvc.perform(get("/mongo/stores/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS.getCode()))
                .andExpect(jsonPath("$.result[0].storeName").value("Test Store"));
        }

        @Test
        @WithMockUser
        @DisplayName("성공: 검색 결과 없음")
        void searchStores_NotFound() throws Exception {
            String keyword = "NonExistent";
            when(storeCollectionQueryService.searchStores(anyString())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/mongo/stores/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    @Nested
    @DisplayName("가게 상세 조회 API [/mongo/stores/{storeKey}] 테스트")
    class GetStoreDetailsTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 가게 키로 상세 정보 조회")
        void getStoreByKey_Success() throws Exception {
            String storeKey = UUID.randomUUID().toString();
            StoreCollection testStore = createTestStore(storeKey, "Test Store");
            when(storeCollectionQueryService.findStoreByStoreKey(anyString())).thenReturn(Optional.of(testStore));

            mockMvc.perform(get("/mongo/stores/{storeKey}", storeKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.storeKey").value(storeKey))
                .andExpect(jsonPath("$.result.menus[0].name").value("Test Menu"));
        }

        @Test
        @WithMockUser
        @DisplayName("실패: 존재하지 않는 가게 키")
        void getStoreByKey_NotFound() throws Exception {
            String storeKey = "non-existent-key";
            when(storeCollectionQueryService.findStoreByStoreKey(anyString())).thenReturn(Optional.empty());

            mockMvc.perform(get("/mongo/stores/{storeKey}", storeKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }
    
    @Nested
    @DisplayName("전체 가게 목록 조회 API [/mongo/stores] 테스트")
    class GetAllStoresTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 전체 가게 목록 조회")
        void getAllStores_Success() throws Exception {
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Store");
            Page<StoreCollection> pagedResponse = new PageImpl<>(Collections.singletonList(testStore));
            when(storeCollectionQueryService.findAllStores(any(Pageable.class))).thenReturn(pagedResponse);

            mockMvc.perform(get("/mongo/stores").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].storeName").value("Test Store"));
        }

        @Test
        @WithMockUser
        @DisplayName("성공: 가게 목록 없음")
        void getAllStores_NotFound() throws Exception {
            Page<StoreCollection> pagedResponse = new PageImpl<>(Collections.emptyList());
            when(storeCollectionQueryService.findAllStores(any(Pageable.class))).thenReturn(pagedResponse);

            mockMvc.perform(get("/mongo/stores").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    @Nested
    @DisplayName("가게 이름 검색 API [/mongo/stores/search-by-name] 테스트")
    class SearchStoresByNameTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 가게 이름으로 검색")
        void searchStoresByName_Success() throws Exception {
            String keyword = "Test";
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Store");
            when(storeCollectionQueryService.searchStoresByName(keyword)).thenReturn(Collections.singletonList(testStore));

            mockMvc.perform(get("/mongo/stores/search-by-name").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].storeName").value("Test Store"));
        }
    }

    @Nested
    @DisplayName("카테고리 검색 API [/mongo/stores/search-by-category] 테스트")
    class SearchStoresByCategoryTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 카테고리로 검색")
        void searchStoresByCategory_Success() throws Exception {
            String category = "CHICKEN";
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Chicken Store");
            when(storeCollectionQueryService.searchStoresByCategory(category)).thenReturn(Collections.singletonList(testStore));

            mockMvc.perform(get("/mongo/stores/search-by-category").param("category", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].categoryKeys[0]").value(category));
        }
    }

    @Nested
    @DisplayName("메뉴 이름 검색 API [/mongo/stores/search-by-menu] 테스트")
    class SearchStoresByMenuNameTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 메뉴 이름으로 검색")
        void searchStoresByMenuName_Success() throws Exception {
            String keyword = "Test Menu";
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Store");
            when(storeCollectionQueryService.searchStoresByMenuName(keyword)).thenReturn(Collections.singletonList(testStore));

            mockMvc.perform(get("/mongo/stores/search-by-menu").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].menus[0].name").value(keyword));
        }
    }

    @Nested
    @DisplayName("메뉴 이름 필터링 API [/mongo/stores/filter-by-menu] 테스트")
    class FilterStoresByMenuNameTest {

        @Test
        @WithMockUser
        @DisplayName("성공: 메뉴 이름으로 필터링")
        void filterStoresByMenuName_Success() throws Exception {
            String keyword = "Test Menu";
            StoreCollection testStore = createTestStore(UUID.randomUUID().toString(), "Test Store");
            when(storeCollectionQueryService.filterStoresByMenuNameAndReturnFilteredMenus(keyword))
                .thenReturn(Collections.singletonList(testStore));

            mockMvc.perform(get("/mongo/stores/filter-by-menu").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].menus[0].name").value(keyword));
        }
    }
}