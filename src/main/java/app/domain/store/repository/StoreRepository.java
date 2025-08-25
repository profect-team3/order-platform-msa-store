package app.domain.store.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.status.StoreAcceptStatus;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

	// Optional<Store> findByUser_UserId(Long userId);

	boolean existsByStoreNameAndRegion(String storeName, Region region);

	Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

	Optional<Store> findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(UUID storeId, StoreAcceptStatus status);

	boolean existsByStoreId(UUID storeId);
	// List<Store> user(User user);

	boolean existsByStoreIdAndUserId(UUID storeId, Long userId);

	Page<Store> findAllByStoreAcceptStatus(StoreAcceptStatus status, Pageable pageable);

	Page<Store> findByStoreAcceptStatusAndStoreNameContainingIgnoreCase(
		StoreAcceptStatus status, String keyword, Pageable pageable);

	Page<Store> findByStoreAcceptStatusAndDeletedAtIsNull(
		StoreAcceptStatus status, Pageable pageable);
}
