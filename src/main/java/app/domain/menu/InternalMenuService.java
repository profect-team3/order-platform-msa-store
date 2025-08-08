package app.domain.menu;

import app.domain.menu.model.dto.response.MenuInfoResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalMenuService {

	private final MenuRepository menuRepository;

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
}