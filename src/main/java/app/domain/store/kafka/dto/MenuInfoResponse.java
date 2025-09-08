package app.domain.store.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuInfoResponse {
    private String name;
    private Long price;
    private int quantity;
}