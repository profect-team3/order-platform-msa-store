package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import app.domain.mongo.StoreCollectionQueryService;
import app.domain.mongo.model.entity.MenuCollection;
import app.domain.mongo.model.entity.StoreCollection;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreCollectionQueryService 테스트")
public class StoreCollectionQueryServiceTest {

    @InjectMocks
    private StoreCollectionQueryService storeCollectionQueryService;

    @Mock
    private MongoTemplate mongoTemplate;

    private StoreCollection createTestStore(String storeKey, String storeName, boolean isHiddenMenu) {
        MenuCollection menu = new MenuCollection(UUID.randomUUID().toString(), "Test Menu", 10000, "A delicious test menu", "CHICKEN", isHiddenMenu);
        return new StoreCollection(
            UUID.randomUUID().toString(),
            1L,
            storeKey,
            storeName,
            "Test Description",
            List.of("CHICKEN"),
            4.5,
            10L,
            "010-1234-5678",
            10000L,
            "Test Address",
            "GANGNAM",
            "SEOUL GANGNAM",
            "APPROVED",
            true,
            null,
            null,
            null,
            1L,
            List.of(menu)
        );
    }

    @Test
    @DisplayName("통합 검색(searchStores) 테스트")
    void searchStoresTest() {
        String keyword = "Test";
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "Test Store", false);
        when(mongoTemplate.find(any(Query.class), eq(StoreCollection.class))).thenReturn(List.of(store));

        List<StoreCollection> result = storeCollectionQueryService.searchStores(keyword);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(StoreCollection.class));
        Query capturedQuery = queryCaptor.getValue();

        String expectedQuery = "{\"$and\":[{\"$or\":[{\"storeName\":{\"$regularExpression\":{\"pattern\":\"Test\",\"options\":\"i\"}}},{\"categoryKeys\":{\"$regularExpression\":{\"pattern\":\"Test\",\"options\":\"i\"}}},{\"menus.name\":{\"$regularExpression\":{\"pattern\":\"Test\",\"options\":\"i\"}}}]},{\"isActive\":true}]}";
        assertEquals(expectedQuery, capturedQuery.getQueryObject().toJson().replaceAll("\\s", ""));
        assertEquals(1, result.size());
        assertEquals("Test Store", result.get(0).getStoreName());
    }

    @Test
    @DisplayName("가게 키로 조회(findStoreByStoreKey) 테스트 - 숨김 처리된 메뉴 필터링")
    void findStoreByStoreKey_FilterHiddenMenus() {
        String storeKey = UUID.randomUUID().toString();
        StoreCollection storeWithHiddenMenu = createTestStore(storeKey, "Test Store", true);
        when(mongoTemplate.findOne(any(Query.class), eq(StoreCollection.class))).thenReturn(storeWithHiddenMenu);

        Optional<StoreCollection> result = storeCollectionQueryService.findStoreByStoreKey(storeKey);

        assertTrue(result.isPresent());
        assertTrue(result.get().getMenus().isEmpty());
    }

    @Test
    @DisplayName("가게 키로 조회(findStoreByStoreKey) 테스트 - 메뉴 없음")
    void findStoreByStoreKey_NoMenus() {
        String storeKey = UUID.randomUUID().toString();
        StoreCollection storeWithoutMenu = createTestStore(storeKey, "Test Store", false);
        storeWithoutMenu.setMenus(null);
        when(mongoTemplate.findOne(any(Query.class), eq(StoreCollection.class))).thenReturn(storeWithoutMenu);

        Optional<StoreCollection> result = storeCollectionQueryService.findStoreByStoreKey(storeKey);

        assertTrue(result.isPresent());
        assertEquals(null, result.get().getMenus());
    }
    
    @Test
    @DisplayName("가게 이름으로 검색(searchStoresByName) 테스트")
    void searchStoresByNameTest() {
        String keyword = "Store";
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "My Store", false);
        when(mongoTemplate.find(any(Query.class), eq(StoreCollection.class))).thenReturn(List.of(store));

        List<StoreCollection> result = storeCollectionQueryService.searchStoresByName(keyword);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(StoreCollection.class));
        Query capturedQuery = queryCaptor.getValue();

        String expectedQuery = "{\"storeName\":{\"$regularExpression\":{\"pattern\":\"Store\",\"options\":\"i\"}}}";
        assertEquals(expectedQuery, capturedQuery.getQueryObject().toJson().replaceAll("\\s", ""));
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("카테고리로 검색(searchStoresByCategory) 테스트")
    void searchStoresByCategoryTest() {
        String category = "CHICKEN";
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "Chicken Store", false);
        when(mongoTemplate.find(any(Query.class), eq(StoreCollection.class))).thenReturn(List.of(store));

        List<StoreCollection> result = storeCollectionQueryService.searchStoresByCategory(category);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(StoreCollection.class));
        Query capturedQuery = queryCaptor.getValue();

        String expectedQuery = "{\"categoryKeys\":{\"$regularExpression\":{\"pattern\":\"CHICKEN\",\"options\":\"i\"}}}";
        assertEquals(expectedQuery, capturedQuery.getQueryObject().toJson().replaceAll("\\s", ""));
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("메뉴 이름으로 검색(searchStoresByMenuName) 테스트")
    void searchStoresByMenuNameTest() {
        String menuName = "Test Menu";
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "My Store", false);
        when(mongoTemplate.find(any(Query.class), eq(StoreCollection.class))).thenReturn(List.of(store));

        List<StoreCollection> result = storeCollectionQueryService.searchStoresByMenuName(menuName);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(StoreCollection.class));
        Query capturedQuery = queryCaptor.getValue();

        String expectedQuery = "{\"menus.name\":{\"$regularExpression\":{\"pattern\":\"Test Menu\",\"options\":\"i\"}}}";
        assertEquals(expectedQuery, capturedQuery.getQueryObject().toJson().replaceAll("\\s", ""));
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("메뉴 이름으로 필터링(filterStoresByMenuNameAndReturnFilteredMenus) 테스트")
    void filterStoresByMenuNameAndReturnFilteredMenusTest() {
        String menuName = "Test Menu";
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "My Store", false);
        @SuppressWarnings("unchecked")
        AggregationResults<StoreCollection> aggregationResults = new AggregationResults<>(List.of(store), new Document());
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(StoreCollection.class))).thenReturn(aggregationResults);

        List<StoreCollection> result = storeCollectionQueryService.filterStoresByMenuNameAndReturnFilteredMenus(menuName);

        ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(aggregationCaptor.capture(), eq("storeCollection"), eq(StoreCollection.class));
        Aggregation capturedAggregation = aggregationCaptor.getValue();

        String expectedAggregation = """
            {"aggregate":"storeCollection","pipeline":[{"$match":{"menus.name":{"$regularExpression":{"pattern":"TestMenu","options":"i"}}}},{"$project":{"storeName":1,"description":1,"avgRating":1,"phoneNumber":1,"minOrderAmount":1,"address":1,"menus":{"$filter":{"input":"$menus","as":"menu","cond":{"$regexMatch":{"input":"$menu.name","regex":"TestMenu","options":"i"}}}}}}]}""";
        assertEquals(expectedAggregation, capturedAggregation.toString().replaceAll("__collection__", "storeCollection").replaceAll("\\s", ""));
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("전체 가게 조회(findAllStores) 테스트")
    void findAllStoresTest() {
        Pageable pageable = PageRequest.of(0, 10);
        StoreCollection store = createTestStore(UUID.randomUUID().toString(), "Test Store", false);
        List<StoreCollection> storeList = List.of(store);
        when(mongoTemplate.find(any(Query.class), eq(StoreCollection.class))).thenReturn(storeList);
        when(mongoTemplate.count(any(Query.class), eq(StoreCollection.class))).thenReturn((long) storeList.size());

        Page<StoreCollection> result = storeCollectionQueryService.findAllStores(pageable);

        verify(mongoTemplate).find(any(Query.class), eq(StoreCollection.class));
        verify(mongoTemplate).count(any(Query.class), eq(StoreCollection.class));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
