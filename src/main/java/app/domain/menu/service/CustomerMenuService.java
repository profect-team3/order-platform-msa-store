package app.domain.menu.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.commonUtil.apiPayload.PagedResponse;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.menu.model.dto.response.GetMenuListResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerMenuService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public PagedResponse<GetMenuListResponse> getMenusByStoreId(UUID storeId, Pageable pageable) {
		boolean exists = storeRepository.existsByStoreId(storeId);
		if (!exists) {
			throw new GeneralException(ErrorStatus.STORE_NOT_FOUND);
		}
		Page<Menu> menuPage = menuRepository.findByStoreStoreIdAndHiddenIsFalse(storeId, pageable);
		Page<GetMenuListResponse> mapped = menuPage.map(GetMenuListResponse::from);

		return PagedResponse.from(mapped);
	}
}