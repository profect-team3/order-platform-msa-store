package app.domain.mongo.model.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import app.domain.mongo.model.entity.MongoMenu;

public interface MongoMenuRepository extends MongoRepository<MongoMenu, String> {
	List<MongoMenu> findByStoreIdAndDeletedAtIsNull(String storeId);
}