package app.domain.menu.model.dto.request;

import lombok.Getter;

import java.util.UUID;

@Getter
public class StockRequest {

    private UUID menuId;
    private Integer quantity;
}
