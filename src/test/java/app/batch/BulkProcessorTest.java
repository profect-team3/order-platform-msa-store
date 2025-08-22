package app.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.batch.dto.BulkDto;
import app.domain.batch.job.BulkProcessor;
import app.domain.mongo.model.entity.StoreCollection;

@DisplayName("BulkProcessor 단위 테스트")
public class BulkProcessorTest {

    private BulkProcessor bulkProcessor;

    @BeforeEach
    void setUp() {
        bulkProcessor = new BulkProcessor(new ObjectMapper());
    }

    private BulkDto createTestBulkDto(String status, String menuJson) {
        BulkDto dto = new BulkDto();
        dto.setStoreKey(UUID.randomUUID());
        dto.setUserId(1L);
        dto.setStoreName("Test Store");
        dto.setDescription("Test Description");
        dto.setCategoryKeys(List.of("CHICKEN"));
        dto.setReviewCount(10L);
        dto.setAvgRating(4.5);
        dto.setPhoneNumber("010-1234-5678");
        dto.setMinOrderAmount(10000L);
        dto.setAddress("Test Address");
        dto.setRegionName("GANGNAM");
        dto.setRegionFullName("SEOUL GANGNAM");
        dto.setStoreAcceptStatus(status);
        dto.setCreatedAt(Timestamp.from(Instant.now()));
        dto.setUpdatedAt(Timestamp.from(Instant.now()));
        dto.setMenuJson(menuJson);
        return dto;
    }

    @Nested
    @DisplayName("process() 메소드 테스트")
    class ProcessTest {

        @Test
        @DisplayName("성공: 유효한 DTO를 StoreCollection으로 변환")
        void process_ValidDto_ShouldConvert() throws Exception {
            String menuJson = """
                [{"menuId":"menu-1","name":"Test Menu","price":12000,"description":"Desc","isHidden":false}]
                """;
            BulkDto dto = createTestBulkDto("APPROVE", menuJson);

            StoreCollection result = bulkProcessor.process(dto);

            assertNotNull(result);
            assertEquals(dto.getStoreKey().toString(), result.getStoreKey());
            assertEquals(dto.getStoreName(), result.getStoreName());
            assertTrue(result.getIsActive());
            assertFalse(result.getMenus().isEmpty());
            assertEquals(1, result.getMenus().size());
            assertEquals("Test Menu", result.getMenus().get(0).getName());
        }

        @Test
        @DisplayName("성공: status가 APPROVE가 아니면 isActive는 false")
        void process_StatusNotApprove_IsActiveShouldBeFalse() throws Exception {
            BulkDto dto = createTestBulkDto("PENDING", "[]");

            StoreCollection result = bulkProcessor.process(dto);

            assertNotNull(result);
            assertFalse(result.getIsActive());
        }

        @Test
        @DisplayName("성공: menuJson이 null이면 메뉴는 빈 리스트")
        void process_NullMenuJson_ShouldHaveEmptyMenuList() throws Exception {
            BulkDto dto = createTestBulkDto("APPROVE", null);

            StoreCollection result = bulkProcessor.process(dto);

            assertNotNull(result);
            assertTrue(result.getMenus().isEmpty());
        }

        @Test
        @DisplayName("성공: menuJson이 비어있으면 메뉴는 빈 리스트")
        void process_EmptyMenuJson_ShouldHaveEmptyMenuList() throws Exception {
            BulkDto dto = createTestBulkDto("APPROVE", "[]");

            StoreCollection result = bulkProcessor.process(dto);

            assertNotNull(result);
            assertTrue(result.getMenus().isEmpty());
        }

        @Test
        @DisplayName("성공: menuJson 형식이 잘못되면 메뉴는 빈 리스트")
        void process_InvalidMenuJson_ShouldHaveEmptyMenuList() throws Exception {
            BulkDto dto = createTestBulkDto("APPROVE", "invalid-json");

            StoreCollection result = bulkProcessor.process(dto);

            assertNotNull(result);
            assertTrue(result.getMenus().isEmpty());
        }
    }
}
