package app.domain.mongo.model.repository;

import app.domain.mongo.model.entity.MongoStore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoStoreRepository extends MongoRepository<MongoStore, String> {
}
