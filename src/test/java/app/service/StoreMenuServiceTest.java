package app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.store.client.UserClient;
import app.domain.menu.service.StoreMenuService;
import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.menu.status.StoreMenuErrorCode;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.exception.GeneralException;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.response.MenuListResponse;

@ExtendWith(MockitoExtension.class)
class StoreMenuServiceTest {

    @InjectMocks
    private StoreMenuService storeMenuService;

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserClient userClient;

    private Long testUserId;
    private UUID testStoreId;
    private UUID testMenuId;
    private Store mockStore;
    private Menu mockMenu;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testStoreId = UUID.randomUUID();
        testMenuId = UUID.randomUUID();

        mockStore = new Store(testStoreId, testUserId, null, null, "Test Store", "Desc", "Addr", "Phone", 10000L, StoreAcceptStatus.APPROVE);
        mockMenu = new Menu(testMenuId, mockStore, "Test Menu", 5000L, "Menu Desc", false, null);
    }

    @Test
    @DisplayName("메뉴 생성 성공")
    void createMenu_Success() {
        MenuCreateRequest request = new MenuCreateRequest(testStoreId, "New Menu", 10000L, "New Menu Desc");
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(menuRepository.existsByStoreAndNameAndDeletedAtIsNull(mockStore, "New Menu")).thenReturn(false);
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);

        MenuCreateResponse response = storeMenuService.createMenu(request, testUserId);

        assertNotNull(response);
        assertEquals(mockMenu.getMenuId(), response.getMenuId());
        assertEquals(mockMenu.getName(), response.getName());
        verify(storeRepository).findById(testStoreId);
        verify(menuRepository).existsByStoreAndNameAndDeletedAtIsNull(mockStore, "New Menu");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 생성 실패 - 가게 없음")
    void createMenu_StoreNotFound() {
        MenuCreateRequest request = new MenuCreateRequest(UUID.randomUUID(), "New Menu", 10000L, "New Menu Desc");
        when(storeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeMenuService.createMenu(request, testUserId));

        assertEquals(StoreMenuErrorCode.STORE_NOT_FOUND_FOR_MENU.getCode(), exception.getErrorReason().getCode());
        verify(storeRepository).findById(any(UUID.class));
        verifyNoInteractions(menuRepository);
    }

    @Test
    @DisplayName("메뉴 생성 실패 - 권한 없음")
    void createMenu_InvalidUserRole() {
        MenuCreateRequest request = new MenuCreateRequest(testStoreId, "New Menu", 10000L, "New Menu Desc");
        Store anotherUserStore = new Store(testStoreId, 99L, null, null, "Another Store", "Desc", "Addr", "Phone", 10000L, StoreAcceptStatus.APPROVE);
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(anotherUserStore));

        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeMenuService.createMenu(request, testUserId));

        assertEquals(StoreErrorCode.INVALID_USER_ROLE.getCode(), exception.getErrorReason().getCode());
        verify(storeRepository).findById(testStoreId);
        verifyNoInteractions(menuRepository);
    }

    @Test
    @DisplayName("메뉴 생성 실패 - 메뉴 이름 중복")
    void createMenu_DuplicateMenuName() {
        MenuCreateRequest request = new MenuCreateRequest(testStoreId, "Existing Menu", 10000L, "Menu Desc");
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(menuRepository.existsByStoreAndNameAndDeletedAtIsNull(mockStore, "Existing Menu")).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class, () ->
            storeMenuService.createMenu(request, testUserId));

        assertEquals(StoreMenuErrorCode.MENU_NAME_DUPLICATE.getCode(), exception.getErrorReason().getCode());
        verify(storeRepository).findById(testStoreId);
        verify(menuRepository).existsByStoreAndNameAndDeletedAtIsNull(mockStore, "Existing Menu");
        verifyNoMoreInteractions(menuRepository);
    }

    @Test
    @DisplayName("메뉴 업데이트 성공")
    void updateMenu_Success() {
        MenuUpdateRequest request = new MenuUpdateRequest(testMenuId, "Updated Menu", 12000L, "Updated Desc", true);
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(testMenuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.existsByStoreAndNameAndDeletedAtIsNull(mockStore, "Updated Menu")).thenReturn(false);
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);

        MenuUpdateResponse response = storeMenuService.updateMenu(request, testUserId);

        assertNotNull(response);
        assertEquals(mockMenu.getMenuId(), response.getMenuId());
        assertEquals(mockMenu.getName(), response.getName());
        verify(menuRepository).findByMenuIdAndDeletedAtIsNull(testMenuId);
        verify(menuRepository).existsByStoreAndNameAndDeletedAtIsNull(mockStore, "Updated Menu");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu_Success() {
        MenuDeleteRequest request = new MenuDeleteRequest(testMenuId);
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(testMenuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);

        MenuDeleteResponse response = storeMenuService.deleteMenu(request, testUserId);

        assertNotNull(response);
        assertEquals(mockMenu.getMenuId(), response.getMenuId());
        assertEquals("DELETED", response.getStatus());
        verify(menuRepository).findByMenuIdAndDeletedAtIsNull(testMenuId);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 가시성 업데이트 성공")
    void updateMenuVisibility_Success() {
        Boolean newVisibility = true;
        when(menuRepository.findByMenuIdAndDeletedAtIsNull(testMenuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);

        MenuUpdateResponse response = storeMenuService.updateMenuVisibility(testMenuId, newVisibility, testUserId);
        assertNotNull(response);
        assertEquals(mockMenu.getMenuId(), response.getMenuId());
        assertEquals(mockMenu.getName(), response.getName());
        verify(menuRepository).findByMenuIdAndDeletedAtIsNull(testMenuId);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공")
    void getMenuList_Success() {
        MenuListRequest request = new MenuListRequest(testStoreId);
        List<Menu> menus = new ArrayList<>();
        menus.add(mockMenu);
        when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
        when(menuRepository.findByStoreAndDeletedAtIsNull(mockStore)).thenReturn(menus);

        MenuListResponse response = storeMenuService.getMenuList(request);

        assertNotNull(response);
        assertEquals(testStoreId, response.getStoreId());
        assertFalse(response.getMenus().isEmpty());
        assertEquals(1, response.getMenus().size());
        assertEquals(mockMenu.getMenuId(), response.getMenus().get(0).getMenuId());
        verify(storeRepository).findById(testStoreId);
        verify(menuRepository).findByStoreAndDeletedAtIsNull(mockStore);
    }
}
