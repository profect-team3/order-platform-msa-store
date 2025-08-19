package app.domain.batch.repository;

import app.domain.batch.dto.BulkDto;
import java.util.List;
import java.util.UUID;

public interface BulkRepository {
    List<BulkDto> findStoresWithDetailsCursor(UUID lastStoreId, int limit);
}