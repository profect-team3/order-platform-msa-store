package app.domain.mongo;

import app.domain.mongo.model.entity.StoreCollection;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreCollectionQueryService {

    private final MongoTemplate mongoTemplate;

    public List<StoreCollection> searchStores(String keyword) {
        Query query = new Query();
        Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("storeName").regex(keyword, "i"),
                Criteria.where("categoryKeys").regex(keyword, "i"),
                Criteria.where("menus.name").regex(keyword, "i")
        );
        Criteria activeCriteria = Criteria.where("isActive").is(true);
        Criteria combinedCriteria = new Criteria().andOperator(searchCriteria, activeCriteria);
        query.addCriteria(combinedCriteria);
        return mongoTemplate.find(query, StoreCollection.class);
    }

    public Optional<StoreCollection> findStoreById(String storeId) {
        StoreCollection store = mongoTemplate.findById(storeId, StoreCollection.class);

        if (store != null && store.getMenus() != null) {
            store.setMenus(store.getMenus().stream()
                .filter(menu -> !menu.isHidden())
                .collect(java.util.stream.Collectors.toList()));
        }

        return Optional.ofNullable(store);
    }

    public List<StoreCollection> searchStoresByName(String storeNameKeyword) {
        Query query = new Query();
        query.addCriteria(Criteria.where("storeName").regex(storeNameKeyword, "i"));
        return mongoTemplate.find(query, StoreCollection.class);
    }

    public List<StoreCollection> searchStoresByCategory(String category) {
        Query query = new Query();
        query.addCriteria(Criteria.where("categoryKeys").regex(category, "i"));
        return mongoTemplate.find(query, StoreCollection.class);
    }

    public List<StoreCollection> searchStoresByMenuName(String menuNameKeyword) {
        Query query = new Query();
        query.addCriteria(Criteria.where("menus.name").regex(menuNameKeyword, "i"));
        return mongoTemplate.find(query, StoreCollection.class);
    }

    public List<StoreCollection> filterStoresByMenuNameAndReturnFilteredMenus(String menuNameKeyword) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("menus.name").regex(menuNameKeyword, "i"));

        ProjectionOperation projectionOperation = Aggregation.project(
                        "storeName", "description", "avgRating", "phoneNumber", "minOrderAmount", "address"
                )
                .and(context -> new Document("$filter",
                        new Document("input", "$menus")
                                .append("as", "menu")
                                .append("cond", new Document("$regexMatch",
                                        new Document("input", "$$menu.name")
                                                .append("regex", menuNameKeyword)
                                                .append("options", "i")
                                ))
                )).as("menus");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);

        return mongoTemplate.aggregate(aggregation, "storeCollection", StoreCollection.class).getMappedResults();
    }

    public List<StoreCollection> findAllStores() {
        return mongoTemplate.findAll(StoreCollection.class);
    }
}