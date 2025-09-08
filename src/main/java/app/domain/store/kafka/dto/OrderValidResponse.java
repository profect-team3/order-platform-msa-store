package app.domain.store.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderValidResponse {
    private UUID orderId;
    private String eventType;
    private Map<UUID, MenuInfoResponse> menuInfos;
}