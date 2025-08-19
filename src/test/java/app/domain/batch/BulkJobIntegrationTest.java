// package app.domain.batch;
//
// import app.domain.menu.model.entity.Category;
// import app.domain.menu.model.entity.Menu;
// import app.domain.mongo.model.entity.StoreCollection;
// import app.domain.store.model.entity.Region;
// import app.domain.store.model.entity.Review;
// import app.domain.store.model.entity.Store;
// import app.domain.store.status.StoreAcceptStatus;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.batch.core.Job;
// import org.springframework.batch.core.JobExecution;
// import org.springframework.batch.core.JobParameters;
// import org.springframework.batch.core.JobParametersBuilder;
// import org.springframework.batch.test.JobLauncherTestUtils;
// import org.springframework.batch.test.context.SpringBatchTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.data.mongodb.core.MongoTemplate;
//
// import jakarta.persistence.EntityManager;
// import jakarta.transaction.Transactional;
// import java.util.List;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.springframework.batch.core.BatchStatus.COMPLETED;
//
// @SpringBootTest
// @SpringBatchTest
// @Transactional
// class BulkJobIntegrationTest {
//
//     @Autowired
//     private JobLauncherTestUtils jobLauncherTestUtils;
//
//     @Autowired
//     private MongoTemplate mongoTemplate;
//
//     @Autowired
//     private EntityManager em;
//
//     @Autowired
//     @Qualifier("storeBatchJob")
//     private Job storeBatchJob;
//
//     private Region region;
//     private Category category;
//
//     @BeforeEach
//     void setUp() {
//         jobLauncherTestUtils.setJob(storeBatchJob);
//         mongoTemplate.dropCollection("stores");
//
//         // 테스트 데이터 설정
//         region = Region.builder().regionName("서울").fullName("서울특별시 강남구").build();
//         em.persist(region);
//
//         category = Category.builder().categoryName("치킨").build();
//         em.persist(category);
//
//         Store store1 = createStore("성공-치킨집", 1L);
//         Store store2 = createStore("완료-피자집", 2L);
//
//         em.persist(createMenu(store1, "후라이드", 18000L));
//         em.persist(createMenu(store1, "양념치킨", 19000L));
//         em.persist(createMenu(store2, "페퍼로니", 20000L));
//
//         em.persist(createReview(store1, 5L));
//         em.persist(createReview(store1, 4L));
//         em.persist(createReview(store2, 3L));
//
//         em.flush();
//         em.clear();
//     }
//
//     @AfterEach
//     void tearDown() {
//         mongoTemplate.dropCollection("stores");
//     }
//
//     @Test
//     @DisplayName("storeBatchJob 전체 실행 테스트 - 성공")
//     void testStoreBatchJob_Success() throws Exception {
//         // given
//         JobParameters jobParameters = new JobParametersBuilder()
//                 .addString("JobID", String.valueOf(System.currentTimeMillis()))
//                 .toJobParameters();
//
//         // when
//         JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
//
//         // then
//         assertEquals(COMPLETED, jobExecution.getStatus());
//
//         List<StoreCollection> results = mongoTemplate.findAll(StoreCollection.class, "stores");
//         assertEquals(2, results.size());
//
//         StoreCollection chickenStore = results.stream().filter(s -> s.getStoreName().equals("성공-치킨집")).findFirst().orElse(null);
//         assertNotNull(chickenStore);
//         assertEquals(2, chickenStore.getReviewCount());
//         assertEquals(4.5, chickenStore.getAvgRating(), 0.01);
//         assertEquals(2, chickenStore.getMenus().size());
//         assertEquals("후라이드", chickenStore.getMenus().get(0).getName());
//     }
//
//     private Store createStore(String name, Long userId) {
//         Store store = Store.builder()
//                 .storeName(name)
//                 .userId(userId)
//                 .address("서울시 강남구")
//                 .storeAcceptStatus(StoreAcceptStatus.APPROVE)
//                 .region(region)
//                 .category(category)
//                 .build();
//         em.persist(store);
//         return store;
//     }
//
//     private Menu createMenu(Store store, String name, Long price) {
//         return Menu.builder()
//                 .store(store)
//                 .name(name)
//                 .price(price)
//                 .description("맛있는 메뉴")
//                 .build();
//     }
//
//     private Review createReview(Store store, long rating) {
//         return Review.builder()
//                 .store(store.getStoreId())
//                 .rating(rating)
//                 .content("리뷰 내용")
//                 .build();
//     }
// }
