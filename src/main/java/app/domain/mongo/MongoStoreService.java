package app.domain.mongo;

import app.domain.mongo.model.entity.MongoMenu;
import app.domain.mongo.model.entity.MongoStore;
import app.domain.mongo.model.repository.MongoStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MongoStoreService {

    private final MongoStoreRepository mongoStoreRepository;

    public Optional<MongoStore> getStoreById(String storeId) {
        return mongoStoreRepository.findById(storeId);
    }

    public List<MongoStore> getAllStores() {
        return mongoStoreRepository.findAll();
    }

    public List<MongoMenu> getMenusFromStore(String storeId) {
        return mongoStoreRepository.findById(storeId)
            .map(MongoStore::getMenus)
            .orElse(List.of());
    }

    public MongoStore saveStoreForRead(MongoStore store) {
        return mongoStoreRepository.save(store);
    }

    public MongoStore createStore(MongoStore mongoStore) {
        if (mongoStore.getMenus() == null) {
            mongoStore.setMenus(new ArrayList<>());
        }
        return mongoStoreRepository.save(mongoStore);
    }

    public MongoMenu addMenuToStore(String storeId, MongoMenu mongoMenu) {
        Optional<MongoStore> optionalStore = mongoStoreRepository.findById(storeId);

        if (optionalStore.isPresent()) {
            MongoStore store = optionalStore.get();
            if (store.getMenus() == null) {
                store.setMenus(new ArrayList<>());
            }
            store.getMenus().add(mongoMenu);
            mongoStoreRepository.save(store);
            return mongoMenu;
        } else {
            return null;
        }
    }
}
