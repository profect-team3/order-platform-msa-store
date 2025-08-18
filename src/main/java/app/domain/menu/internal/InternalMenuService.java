package app.domain.menu.internal;


import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.model.dto.response.MenuInfoResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.entity.Stock;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.model.repository.StockRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalMenuService {

	private final MenuRepository menuRepository;
	private final StockRepository stockRepository;

	@Transactional(readOnly = true)
	public List<MenuInfoResponse> getMenuInfoList(List<UUID> menuIds) {
		if (menuIds == null || menuIds.isEmpty()) {
			throw new GeneralException(StoreMenuErrorCode.MENU_ID_NULL);
		}

		List<Menu> menus = menuRepository.findAllByMenuIdInAndHiddenIsFalse(menuIds);


		if (menus.size() != menuIds.size()) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
		}

		return menus.stream().map(MenuInfoResponse::from).toList();
	}

	@Retryable(
		value = {ObjectOptimisticLockingFailureException.class},
		maxAttempts = 5,
		backoff = @Backoff(delay = 300, multiplier = 2)
	)
	@Transactional
	public boolean decreaseStock(List<StockRequest> requests) {
		List<UUID> menuIds = requests.stream()
				.map(StockRequest::getMenuId)
				.toList();

		Map<UUID, Stock> stockMap = stockRepository.findByMenuMenuIdIn(menuIds).stream()
				.collect(Collectors.toMap(stock -> stock.getMenu().getMenuId(), Function.identity()));

		boolean allInStock = requests.stream()
				.allMatch(info -> {
					Stock stock = stockMap.get(info.getMenuId());
					return stock != null && stock.getStock() >= info.getQuantity();
				});

		if (allInStock) {
			requests.forEach(info -> {
				Stock stock = stockMap.get(info.getMenuId());
				stock.setStock(stock.getStock() - info.getQuantity());
			});
			return true;
		} else {
			throw new GeneralException(StoreMenuErrorCode.OUT_OF_STOCK);
		}
	}

	@Recover
	public boolean recover(ObjectOptimisticLockingFailureException e, List<StockRequest> requests) {
		log.error("재고 감소 재시도 모두 실패. 요청: {}", requests, e);
		throw new GeneralException(StoreMenuErrorCode.CONCURRENCY_ERROR);
	}
}
