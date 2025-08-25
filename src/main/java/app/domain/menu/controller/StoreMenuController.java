package app.domain.menu.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.commonSecurity.TokenPrincipalParser;
import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.request.MenuVisibleRequest;
import app.domain.menu.model.dto.request.StockRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.service.StoreMenuService;
import app.domain.menu.status.StoreMenuSuccessStatus;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Store", description = "가게, 가게 메뉴 관리")
@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class StoreMenuController {

	private final StoreMenuService storeMenuService;
	private final TokenPrincipalParser tokenPrincipalParser;

	@PostMapping("/menu")
	public ApiResponse<MenuCreateResponse> createMenu(@Valid @RequestBody MenuCreateRequest request, Authentication authentication) {
		MenuCreateResponse response = storeMenuService.createMenu(request, 	Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_CREATED_SUCCESS, response);
	}

	@PutMapping("/menu")
	public ApiResponse<MenuUpdateResponse> updateMenu(@Valid @RequestBody MenuUpdateRequest request, Authentication authentication) {
		MenuUpdateResponse response = storeMenuService.updateMenu(request, 	Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@DeleteMapping("/menu/delete")
	public ApiResponse<MenuDeleteResponse> deleteMenu(@Valid @RequestBody MenuDeleteRequest request, Authentication authentication) {
		MenuDeleteResponse response = storeMenuService.deleteMenu(request,Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_DELETED_SUCCESS, response);
	}

	@PutMapping("/menu/visible")
	public ApiResponse<MenuUpdateResponse> updateMenuVisibility(
		@Valid @RequestBody MenuVisibleRequest request, Authentication authentication) {
		MenuUpdateResponse response = storeMenuService.updateMenuVisibility(request.getMenuId(),
			request.getVisible(), 	Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@GetMapping("/menu")
	public ApiResponse<MenuListResponse> getMenuList(@RequestParam("storeId") UUID storeId) {
		MenuListRequest request = new MenuListRequest(storeId);
		MenuListResponse response = storeMenuService.getMenuList(request);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus._OK, response);
	}

	@PutMapping("/menu/stock")
	public ApiResponse<MenuUpdateResponse> updateStock(@Valid @RequestBody StockRequest request, Authentication authentication) {
		MenuUpdateResponse response = storeMenuService.updateStock(request, Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.STOCK_UPDATED_SUCCESS, response);
	}

}