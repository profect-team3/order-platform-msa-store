package app.domain.store.internal;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalStoreService {

	private final StoreRepository storeRepository;

	public boolean isStoreExists(UUID storeId) {
		return storeRepository.existsById(storeId);
	}

	public boolean isStoreOwner(UUID storeId, Long userId) {
		return storeRepository.existsByStoreIdAndUserId(storeId, userId);
	}
}
