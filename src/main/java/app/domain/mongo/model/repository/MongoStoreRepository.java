package app.domain.mongo.model.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import app.domain.mongo.model.entity.MongoStore;

public interface MongoStoreRepository extends MongoRepository<MongoStore, String> {
	Optional<MongoStore> findById(String id);
}