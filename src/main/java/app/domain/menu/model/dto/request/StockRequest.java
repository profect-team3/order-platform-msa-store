package app.domain.menu.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StockRequest {

    private UUID menuId;
    private Long quantity;
}
