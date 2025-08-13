package app.domain.store.internal;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.store.repository.StoreRepository;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalStoreService {

	private final StoreRepository storeRepository;

	public boolean isStoreExists(UUID storeId) {
		try {
			return storeRepository.existsById(storeId);
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public boolean isStoreOwner(UUID storeId, Long userId) {
		try {
			return storeRepository.existsByStoreIdAndUserId(storeId, userId);
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}
