package app.domain.menu.internal;

import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.entity.Stock;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.model.repository.StockRepository;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InternalMenuServiceTest {

    @Autowired
    private InternalMenuService internalMenuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Menu savedMenu;

    @BeforeEach
    void setUp() {
        // 테스트 실행 전, 기존 데이터 삭제
        stockRepository.deleteAll();
        menuRepository.deleteAll();
        storeRepository.deleteAll();
        regionRepository.deleteAll();
        categoryRepository.deleteAll();

        Region region = regionRepository.save(Region.builder()
                .regionCode("test_code")
                .regionName("test region")
                .fullName("test region full name")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .categoryName("test category")
                .build());

        Store store = storeRepository.save(Store.builder()
                .userId(1L)
                .region(region)
                .category(category)
                .storeName("test store")
                .address("test address")
                .build());

        Menu menu = Menu.builder()
                .store(store)
                .name("test menu")
                .price(10000L)
                .build();
        savedMenu = menuRepository.save(menu);

        Stock stock = Stock.builder()
                .menu(savedMenu)
                .stock(100L)
                .build();
        stockRepository.save(stock);
    }

    @Test
    @DisplayName("재고 동시성 테스트")
    void decreaseStock_concurrency_test() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    StockRequest request = new StockRequest(savedMenu.getMenuId(), 1);
                    internalMenuService.decreaseStock(Collections.singletonList(request));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Stock resultStock = stockRepository.findByMenuMenuIdIn(Collections.singletonList(savedMenu.getMenuId())).get(0);
        assertEquals(0, resultStock.getStock(), "재고가 0이 되어야 합니다.");
    }
}