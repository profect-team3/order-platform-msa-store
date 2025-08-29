package app.domain.menu.internal;

import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.status.StoreMenuErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRetryService {

	private final InternalMenuService internalMenuService;

	@Retryable(
		value = {ObjectOptimisticLockingFailureException.class},
		maxAttempts = 5,
		backoff = @Backoff(delay = 300, multiplier = 2)
	)
	public boolean decreaseStock(List<StockRequest> requests) {
		log.info("Attempting to decrease stock for requests: {}", requests);
		return internalMenuService.decreaseStockTransactional(requests);
	}

	@Recover
	public boolean recover(ObjectOptimisticLockingFailureException e, List<StockRequest> requests) {
		log.error("재고 감소 재시도 모두 실패. 요청: {}", requests, e);
		throw new GeneralException(StoreMenuErrorCode.CONCURRENCY_ERROR);
	}
}
