package app.domain.batch.job;

import app.domain.batch.dto.StoreMenuDto;
import app.domain.batch.repository.BatchStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreBatchReader implements ItemReader<StoreMenuDto> {

    private final BatchStoreRepository batchStoreRepository;
    private Iterator<StoreMenuDto> storeIterator;
    private UUID lastStoreId = null;
    private boolean initialized = false;
    private final int batchSize = 100;

    @Override
    public StoreMenuDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }

        if (storeIterator != null && storeIterator.hasNext()) {
            StoreMenuDto current = storeIterator.next();
            lastStoreId = current.getStoreId();
            return current;
        }

        List<StoreMenuDto> nextBatch = loadNextBatch();
        if (!nextBatch.isEmpty()) {
            storeIterator = nextBatch.iterator();
            StoreMenuDto current = storeIterator.next();
            lastStoreId = current.getStoreId();
            return current;
        }

        return null;
    }

    private void initialize() {
        lastStoreId = null; // 첫 번째 조회는 null부터 시작
        List<StoreMenuDto> firstBatch = loadNextBatch();
        if (!firstBatch.isEmpty()) {
            storeIterator = firstBatch.iterator();
        }
        initialized = true;
    }

    private List<StoreMenuDto> loadNextBatch() {
        return batchStoreRepository.findStoresWithDetailsCursor(lastStoreId, batchSize);
    }

    public void reset() {
        initialized = false;
        lastStoreId = null;
        storeIterator = null;
    }
}
