package app.domain.batch.job;

import app.domain.mongo.model.entity.StoreCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class BulkWriter implements ItemWriter<StoreCollection> {

	private final MongoTemplate mongoTemplate;

	@Override
	public void write(Chunk<? extends StoreCollection> chunk) throws Exception {
		if (chunk.isEmpty()) {
			return;
		}

		List<String> storeKeys = chunk.getItems().stream()
				.map(StoreCollection::getStoreKey)
				.toList();

		Query versionQuery = new Query(where("storeKey").in(storeKeys));
		versionQuery.fields().include("storeKey").include("version");

		List<StoreCollection> existingStores = mongoTemplate.find(versionQuery, StoreCollection.class, "stores");

		Map<String, Long> versionMap = existingStores.stream()
				.collect(Collectors.toMap(StoreCollection::getStoreKey, StoreCollection::getVersion));

		for (StoreCollection item : chunk) {
			item.setVersion(versionMap.get(item.getStoreKey()));
		}

		String collectionName = "stores";
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collectionName);

		for (StoreCollection item : chunk) {
			Query query = new Query(where("storeKey").is(item.getStoreKey()).and("version").is(item.getVersion()));

			Update update = new Update();
			update.set("userId", item.getUserId());
			update.set("storeKey", item.getStoreKey());
			update.set("storeName", item.getStoreName());
			update.set("description", item.getDescription());
			update.set("categoryKeys", item.getCategoryKeys());
			update.set("avgRating", item.getAvgRating());
			update.set("phoneNumber", item.getPhoneNumber());
			update.set("minOrderAmount", item.getMinOrderAmount());
			update.set("address", item.getAddress());
			update.set("regionName", item.getRegionName());
			update.set("regionFullName", item.getRegionFullName());
			update.set("storeAcceptStatus", item.getStoreAcceptStatus());
			update.set("isActive", item.getIsActive());
			update.set("createdAt", item.getCreatedAt());
			update.set("updatedAt", item.getUpdatedAt());
			update.set("deletedAt", item.getDeletedAt());
			update.set("menus", item.getMenus());
			update.set("reviewCount", item.getReviewCount());
			update.inc("version", 1);

			bulkOps.upsert(query, update);
		}

		bulkOps.execute();
	}
}