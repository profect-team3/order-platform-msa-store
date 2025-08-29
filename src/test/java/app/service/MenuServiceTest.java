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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import app.commonUtil.apiPayload.PagedResponse;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.store.client.UserClient;
import app.domain.menu.service.CustomerMenuService;
import app.domain.menu.service.StoreMenuService;
import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.response.GetMenuListResponse;
import app.domain.menu.model.dto.response.MenuListResponse;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private StoreMenuService storeMenuService;

    @InjectMocks
    private CustomerMenuService customerMenuService;

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

    // --- StoreMenuService Tests ---

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

    // ... other StoreMenuService tests ...

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

    // --- CustomerMenuService Tests ---

    @Test
    @DisplayName("고객 메뉴 목록 조회 성공")
    void getMenusByStoreId_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Menu menu = new Menu(UUID.randomUUID(), mockStore, "Customer Menu", 9900L, "Desc", false, null);
        Page<Menu> menuPage = new PageImpl<>(List.of(menu), pageable, 1);

        when(storeRepository.existsByStoreId(testStoreId)).thenReturn(true);
        when(menuRepository.findByStoreStoreIdAndHiddenIsFalse(testStoreId, pageable)).thenReturn(menuPage);

        // when
        PagedResponse<GetMenuListResponse> response = customerMenuService.getMenusByStoreId(testStoreId, pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Customer Menu", response.getContent().get(0).getName());
        verify(storeRepository).existsByStoreId(testStoreId);
        verify(menuRepository).findByStoreStoreIdAndHiddenIsFalse(testStoreId, pageable);
    }

    @Test
    @DisplayName("고객 메뉴 목록 조회 실패 - 가게 없음")
    void getMenusByStoreId_StoreNotFound() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(storeRepository.existsByStoreId(testStoreId)).thenReturn(false);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () ->
            customerMenuService.getMenusByStoreId(testStoreId, pageable));

        assertEquals(ErrorStatus.STORE_NOT_FOUND.getCode(), exception.getErrorReason().getCode());
        verify(storeRepository).existsByStoreId(testStoreId);
        verifyNoInteractions(menuRepository);
    }
}
