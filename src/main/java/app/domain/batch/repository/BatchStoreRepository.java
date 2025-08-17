package app.domain.batch.repository;

import app.domain.batch.dto.StoreMenuDto;
import java.util.List;
import java.util.UUID;

public interface BatchStoreRepository {
    List<StoreMenuDto> findStoresWithDetailsCursor(UUID lastStoreId, int limit);
}