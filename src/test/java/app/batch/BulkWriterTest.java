package app.batch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import app.domain.batch.job.BulkWriter;
import app.domain.mongo.model.entity.StoreCollection;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkWriter 단위 테스트")
public class BulkWriterTest {

    @InjectMocks
    private BulkWriter bulkWriter;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private BulkOperations bulkOperations;

    private StoreCollection createTestStoreCollection(String storeKey) {
        StoreCollection store = new StoreCollection();
        store.setStoreKey(storeKey);
        store.setStoreName("Test Store");
        return store;
    }

    @Test
    @DisplayName("성공: chunk 데이터를 bulk upsert로 정상 처리한다")
    void write_WithValidChunk_ShouldPerformBulkUpsert() throws Exception {
        StoreCollection store1 = createTestStoreCollection(UUID.randomUUID().toString());
        StoreCollection store2 = createTestStoreCollection(UUID.randomUUID().toString());
        Chunk<StoreCollection> chunk = new Chunk<>(List.of(store1, store2));

        when(mongoTemplate.bulkOps(any(BulkOperations.BulkMode.class), anyString())).thenReturn(bulkOperations);

        bulkWriter.write(chunk);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

        verify(mongoTemplate, times(1)).bulkOps(BulkOperations.BulkMode.UNORDERED, "stores");
        verify(bulkOperations, times(2)).upsert(queryCaptor.capture(), updateCaptor.capture());
        verify(bulkOperations, times(1)).execute();

        assertEquals(store1.getStoreKey(), queryCaptor.getAllValues().get(0).getQueryObject().getString("storeKey"));
        assertEquals(store2.getStoreKey(), queryCaptor.getAllValues().get(1).getQueryObject().getString("storeKey"));
        assertEquals(store1.getStoreName(), updateCaptor.getAllValues().get(0).getUpdateObject().get("$set", org.bson.Document.class).getString("storeName"));
        assertEquals(store2.getStoreName(), updateCaptor.getAllValues().get(1).getUpdateObject().get("$set", org.bson.Document.class).getString("storeName"));
    }

    @Test
    @DisplayName("성공: 빈 chunk에 대해서는 아무 작업도 수행하지 않는다")
    void write_WithEmptyChunk_ShouldDoNothing() throws Exception {
        Chunk<StoreCollection> emptyChunk = new Chunk<>();

        bulkWriter.write(emptyChunk);

        verify(mongoTemplate, never()).bulkOps(any(), anyString());
        verify(bulkOperations, never()).execute();
    }
}
