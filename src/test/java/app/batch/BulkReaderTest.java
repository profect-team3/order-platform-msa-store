package app.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.batch.dto.BulkDto;
import app.domain.batch.job.BulkReader;
import app.domain.batch.repository.BulkRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkReader 단위 테스트")
public class BulkReaderTest {

    @InjectMocks
    private BulkReader bulkReader;

    @Mock
    private BulkRepository bulkRepository;

    private List<BulkDto> createMockBulkDtos(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                BulkDto dto = new BulkDto();
                dto.setStoreKey(UUID.randomUUID());
                dto.setStoreName("Store " + i);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @BeforeEach
    void setUp() {
        bulkReader.reset();
    }

    @Test
    @DisplayName("첫 read() 호출 시 데이터를 성공적으로 읽어온다")
    void read_FirstCall_ShouldReturnFirstItem() throws Exception {
        List<BulkDto> firstBatch = createMockBulkDtos(5);
        when(bulkRepository.findStoresWithDetailsCursor(isNull(), anyInt())).thenReturn(firstBatch);

        BulkDto result = bulkReader.read();

        assertNotNull(result);
        assertEquals(firstBatch.get(0).getStoreKey(), result.getStoreKey());
        verify(bulkRepository, times(1)).findStoresWithDetailsCursor(isNull(), anyInt());
    }

    @Test
    @DisplayName("한 배치를 모두 읽을 때까지 repository를 추가 호출하지 않는다")
    void read_WithinBatch_ShouldNotCallRepositoryAgain() throws Exception {
        List<BulkDto> firstBatch = createMockBulkDtos(3);
        when(bulkRepository.findStoresWithDetailsCursor(isNull(), anyInt())).thenReturn(firstBatch);

        BulkDto item1 = bulkReader.read();
        BulkDto item2 = bulkReader.read();
        BulkDto item3 = bulkReader.read();

        assertNotNull(item1);
        assertNotNull(item2);
        assertNotNull(item3);
        verify(bulkRepository, times(1)).findStoresWithDetailsCursor(isNull(), anyInt());
    }

    @Test
    @DisplayName("배치가 소진되면 다음 배치를 가져온다")
    void read_WhenBatchExhausted_ShouldFetchNextBatch() throws Exception {
        List<BulkDto> firstBatch = createMockBulkDtos(2);
        List<BulkDto> secondBatch = createMockBulkDtos(2);
        UUID lastStoreKeyFromFirstBatch = firstBatch.get(1).getStoreKey();

        when(bulkRepository.findStoresWithDetailsCursor(isNull(), anyInt())).thenReturn(firstBatch);
        when(bulkRepository.findStoresWithDetailsCursor(eq(lastStoreKeyFromFirstBatch), anyInt())).thenReturn(secondBatch);

        bulkReader.read(); 
        bulkReader.read(); 
        BulkDto firstItemOfSecondBatch = bulkReader.read();

        assertNotNull(firstItemOfSecondBatch);
        assertEquals(secondBatch.get(0).getStoreKey(), firstItemOfSecondBatch.getStoreKey());
        verify(bulkRepository, times(1)).findStoresWithDetailsCursor(isNull(), anyInt());
        verify(bulkRepository, times(1)).findStoresWithDetailsCursor(eq(lastStoreKeyFromFirstBatch), anyInt());
    }

    @Test
    @DisplayName("더 이상 읽을 데이터가 없으면 null을 반환한다")
    void read_WhenNoMoreData_ShouldReturnNull() throws Exception {
        when(bulkRepository.findStoresWithDetailsCursor(isNull(), anyInt())).thenReturn(List.of());

        BulkDto result = bulkReader.read();

        assertNull(result);
    }

    @Test
    @DisplayName("reset() 호출 시 상태가 초기화된다")
    void reset_ShouldResetReaderState() throws Exception {
        List<BulkDto> firstBatch = createMockBulkDtos(2);
        when(bulkRepository.findStoresWithDetailsCursor(isNull(), anyInt())).thenReturn(firstBatch);

        bulkReader.read();
        bulkReader.reset();
        bulkReader.read();

        verify(bulkRepository, times(2)).findStoresWithDetailsCursor(isNull(), anyInt());
    }
}
