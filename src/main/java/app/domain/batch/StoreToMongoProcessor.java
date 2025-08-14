// package app.domain.batch;
//
// import java.time.ZoneId;
// import java.util.Collections;
// import java.util.List;
// import java.util.stream.Collectors;
//
// import org.springframework.batch.item.ItemProcessor;
// import org.springframework.stereotype.Component;
//
// import app.domain.menu.model.entity.Menu;
// import app.domain.menu.model.repository.MenuRepository;
// import app.domain.mongo.model.entity.MongoMenu;
// import app.domain.mongo.model.entity.MongoStore;
// import app.domain.store.model.entity.Store;
// import lombok.RequiredArgsConstructor;
//
// @Component
// @RequiredArgsConstructor
// public class StoreToMongoProcessor implements ItemProcessor<Store, MongoStore> {
//
//     private final MenuRepository menuRepository;
//
//     @Override
//     public MongoStore process(Store store) throws Exception {
//         MongoStore mongoStore = new MongoStore();
//         mongoStore.setId(store.getStoreId().toString());
//         mongoStore.setUserId(store.getUserId());
//         mongoStore.setStoreName(store.getStoreName());
//         mongoStore.setDescription(store.getDescription());
//         mongoStore.setCategoryKeys(Collections.singletonList(store.getCategory().getCategoryName()));
//         mongoStore.setPrimaryCategory(store.getCategory().getCategoryName());
//         mongoStore.setAvgRating(0.0); // 스토어 아이디에 맞는 리뷰도 조회해서 평균평점 구해야함
//         mongoStore.setPhoneNumber(store.getPhoneNumber());
//         mongoStore.setMinOrderAmount(store.getMinOrderAmount());
//         mongoStore.setRegionName(store.getRegion().getRegionName());
//         mongoStore.setRegionFullName(store.getRegion().getFullName());
//         mongoStore.setStoreAcceptStatus(store.getStoreAcceptStatus().name());
//         mongoStore.setIsActive(!store.isDeleted());
//         mongoStore.setCreatedAt(java.util.Date.from(store.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
//         mongoStore.setUpdatedAt(java.util.Date.from(store.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()));
//         mongoStore.setVersion(1L);
//
//         List<Menu> menus = menuRepository.findByStore(store);
//         List<MongoMenu> mongoMenus = menus.stream().map(this::convertToMongoMenu).collect(Collectors.toList());
//         mongoStore.setMenus(mongoMenus);
//
//         return mongoStore;
//     }
//
//     private MongoMenu convertToMongoMenu(Menu menu) {
//         MongoMenu mongoMenu = new MongoMenu();
//         mongoMenu.setMenuId(menu.getMenuId().toString());
//         mongoMenu.setName(menu.getName());
//         mongoMenu.setPrice(menu.getPrice().intValue());
//         mongoMenu.setDescription(menu.getDescription());
//         if (menu.getCategory() != null) {
//             mongoMenu.setCategory(menu.getCategory().getCategoryName());
//         }
//         mongoMenu.setHidden(menu.isHidden());
//         return mongoMenu;
//     }
// }
