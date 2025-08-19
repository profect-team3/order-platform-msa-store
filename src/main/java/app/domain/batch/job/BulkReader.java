package app.domain.batch.job;

import app.domain.batch.dto.BulkDto;
import app.domain.batch.repository.BulkRepository;
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
public class BulkReader implements ItemReader<BulkDto> {

    private final BulkRepository bulkRepository;
    private Iterator<BulkDto> storeIterator;
    private UUID lastStoreId = null;
    private boolean initialized = false;
    private final int batchSize = 100;

    @Override
    public BulkDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }

        if (storeIterator != null && storeIterator.hasNext()) {
            BulkDto current = storeIterator.next();
            lastStoreId = current.getStoreId();
            return current;
        }

        List<BulkDto> nextBatch = loadNextBatch();
        if (!nextBatch.isEmpty()) {
            storeIterator = nextBatch.iterator();
            BulkDto current = storeIterator.next();
            lastStoreId = current.getStoreId();
            return current;
        }

        return null;
    }

    private void initialize() {
        lastStoreId = null; // 첫 번째 조회는 null부터 시작
        List<BulkDto> firstBatch = loadNextBatch();
        if (!firstBatch.isEmpty()) {
            storeIterator = firstBatch.iterator();
        }
        initialized = true;
    }

    private List<BulkDto> loadNextBatch() {
        return bulkRepository.findStoresWithDetailsCursor(lastStoreId, batchSize);
    }

    public void reset() {
        initialized = false;
        lastStoreId = null;
        storeIterator = null;
    }
}
