package app.domain.menu.internal;

import app.commonUtil.apiPayload.ApiResponse;
import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.model.dto.response.MenuInfoResponse;
import app.domain.menu.status.StoreMenuSuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/menus")
@RequiredArgsConstructor
@Tag(name = "내부 API", description = "내부 API")
@Slf4j
public class InternalMenuController {

	private final InternalMenuService internalMenuService;
	private final StockRetryService stockRetryService;

	@PostMapping("/batch")
	public ApiResponse<List<MenuInfoResponse>> getMenuInfoList(@RequestBody List<UUID> menuIds) {
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_INFO_BATCH_SUCCESS,internalMenuService.getMenuInfoList(menuIds));
	}

	@PostMapping("/stocks/decrease")
	public ApiResponse<Boolean> decreaseStock(@RequestBody List<StockRequest> requests) {
		log.info("InternalMenuService class: {}", internalMenuService.getClass().getName());
		boolean result = stockRetryService.decreaseStock(requests);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.STOCK_DECREASE_SUCCESS, result);
	}
}
