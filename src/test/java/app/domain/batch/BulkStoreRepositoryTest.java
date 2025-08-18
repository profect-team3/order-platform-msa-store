package app.domain.batch;

import app.domain.batch.dto.StoreMenuDto;
import app.domain.batch.repository.BatchStoreRepositoryImpl;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Review;
import app.domain.store.model.entity.Store;
import app.domain.store.status.StoreAcceptStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import app.global.config.JpaAuditingConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ObjectMapper.class, JpaAuditingConfig.class})
class BulkStoreRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    private BatchStoreRepositoryImpl batchStoreRepository;

    private Region region;
    private Category category;
    private Store store1, store2;

    @BeforeEach
    void setUp() {
        batchStoreRepository = new BatchStoreRepositoryImpl(em.getEntityManager(), objectMapper);

        region = Region.builder().regionCode("11680").regionName("서울").fullName("서울특별시 강남구").build();
        em.persist(region);

        category = Category.builder().categoryName("치킨").build();
        em.persist(category);

        store1 = createStore("테스트치킨집1", 1L);
        store2 = createStore("테스트치킨집2", 2L);

        em.persist(createMenu(store1, "후라이드", 18000L));
        em.persist(createMenu(store1, "양념치킨", 19000L));
        em.persist(createMenu(store2, "마늘치킨", 20000L));

        em.persist(createReview(store1, 5L));
        em.persist(createReview(store1, 4L));
        em.persist(createReview(store2, 3L));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("가게, 메뉴, 리뷰 정보를 올바르게 조회하는지 테스트")
    void testFindStoresWithDetailsCursor() throws Exception {
        List<StoreMenuDto> results = batchStoreRepository.findStoresWithDetailsCursor(null, 10);

        assertEquals(2, results.size());

        StoreMenuDto dto1 = results.stream().filter(s -> s.getStoreId().equals(store1.getStoreId())).findFirst().orElse(null);
        assertNotNull(dto1);
        assertEquals("테스트치킨집1", dto1.getStoreName());
        assertEquals(2L, dto1.getReviewCount());
        assertEquals(4.5, dto1.getAvgRating(), 0.01);

        List<Map<String, Object>> menus1 = objectMapper.readValue(dto1.getMenuJson(), new TypeReference<>() {});
        assertEquals(2, menus1.size());
        assertTrue(menus1.stream().anyMatch(m -> m.get("name").equals("후라이드")));

        StoreMenuDto dto2 = results.stream().filter(s -> s.getStoreId().equals(store2.getStoreId())).findFirst().orElse(null);
        assertNotNull(dto2);
        assertEquals("테스트치킨집2", dto2.getStoreName());
        assertEquals(1L, dto2.getReviewCount());
        assertEquals(3.0, dto2.getAvgRating(), 0.01);
        List<Map<String, Object>> menus2 = objectMapper.readValue(dto2.getMenuJson(), new TypeReference<>() {});
        assertEquals(1, menus2.size());
        assertEquals("마늘치킨", menus2.get(0).get("name"));
    }

    private Store createStore(String name, Long userId) {
        Store store = Store.builder()
                .storeName(name)
                .userId(userId)
                .address("서울시 강남구")
                .storeAcceptStatus(StoreAcceptStatus.APPROVE)
                .region(region)
                .category(category)
                .build();
        em.persist(store);
        return store;
    }

    private Menu createMenu(Store store, String name, Long price) {
        return Menu.builder()
                .store(store)
                .name(name)
                .price(price)
                .description("맛있는 메뉴")
                .build();
    }

    private Review createReview(Store store, Long rating) {
        return Review.builder()
                .store(store.getStoreId())
                .rating(rating)
                .content("리뷰 내용")
                .build();
    }
}
