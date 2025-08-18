package app.domain.menu;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.entity.Stock;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.model.repository.StockRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreMenuService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;
	private final StockRepository stockRepository;

	@Transactional
	public MenuCreateResponse createMenu(MenuCreateRequest request, Long userId) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreMenuErrorCode.STORE_NOT_FOUND_FOR_MENU));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		if (menuRepository.existsByStoreAndNameAndDeletedAtIsNull(store, request.getName())) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NAME_DUPLICATE);
		}

		Menu menu = new Menu(null, store, request.getName(), request.getPrice(), request.getDescription(), false, null, null);

		Menu savedMenu = menuRepository.save(menu);

		return new MenuCreateResponse(savedMenu.getMenuId(), savedMenu.getName());
	}

	@Transactional
	public MenuUpdateResponse updateMenu(MenuUpdateRequest request, Long userId) {

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(request.getMenuId())
			.orElseThrow(() -> new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUserId().equals(userId)) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
		}

		if (request.getName() != null && !request.getName().equals(menu.getName())) {
			if (menuRepository.existsByStoreAndNameAndDeletedAtIsNull(menu.getStore(), request.getName())) {
				throw new GeneralException(StoreMenuErrorCode.MENU_NAME_DUPLICATE);
			}
		}

		menu.update(request.getName(), request.getPrice(), request.getDescription(), request.getIsHidden());

		Menu updatedMenu = menuRepository.save(menu);

		return new MenuUpdateResponse(updatedMenu.getMenuId(), updatedMenu.getName());
	}

	@Transactional
	public MenuDeleteResponse deleteMenu(MenuDeleteRequest request, Long userId) {

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(request.getMenuId())
			.orElseThrow(() -> new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUserId().equals(userId)) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
		}

		if (menu.getDeletedAt() != null) {
			throw new GeneralException(StoreMenuErrorCode.MENU_ALREADY_DELETED);
		}

		menu.markAsDeleted();
		menuRepository.save(menu);

		return new MenuDeleteResponse(menu.getMenuId(), "DELETED");
	}

	@Transactional
	public MenuUpdateResponse updateMenuVisibility(UUID menuId, Boolean visible, Long userId) {

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)
			.orElseThrow(() -> new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUserId().equals(userId)) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
		}

		menu.update(null, null, null, visible);
		Menu updatedMenu = menuRepository.save(menu);

		return new MenuUpdateResponse(updatedMenu.getMenuId(), updatedMenu.getName());
	}

	@Transactional(readOnly = true)
	public MenuListResponse getMenuList(MenuListRequest request) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		List<Menu> menus = menuRepository.findByStoreAndDeletedAtIsNull(store);

		List<MenuListResponse.MenuDetail> menuDetails = menus.stream()
			.map(menu -> new MenuListResponse.MenuDetail(menu.getMenuId(), menu.getName(), menu.getPrice(),
				menu.getDescription(), menu.isHidden()))
			.collect(Collectors.toList());

		return new MenuListResponse(store.getStoreId(), menuDetails);
	}

	@Transactional
	public MenuUpdateResponse updateStock(StockRequest request, Long userId) {
		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(request.getMenuId())
			.orElseThrow(() -> new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUserId().equals(userId)) {
			throw new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND);
		}

		Stock stock = stockRepository.findByMenu_MenuId(request.getMenuId())
			.orElse(Stock.builder().menu(menu).stock(0L).build());

		stock.setStock(request.getQuantity());
		stockRepository.save(stock);

		return new MenuUpdateResponse(menu.getMenuId(), menu.getName());
	}
}