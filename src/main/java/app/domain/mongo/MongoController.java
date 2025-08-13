package app.domain.mongo;

import app.domain.mongo.model.entity.MongoMenu;
import app.domain.mongo.model.entity.MongoStore;
import app.domain.mongo.status.MongoStoreMenuSuccessCode;
import app.domain.mongo.status.MongoStoreMenuErrorCode; // 에러 코드 추가
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HttpStatus 추가
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Optional 임포트

@RestController
@RequestMapping("/mongo/stores")
@RequiredArgsConstructor
public class MongoController {

    private final MongoStoreService mongoStoreService;

    /**
     * 특정 ID를 가진 상점 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return ApiResponse<MongoStore> 상점 정보 또는 NOT_FOUND 에러
     */
    @GetMapping("/{storeId}")
    public ApiResponse<MongoStore> getStoreById(@PathVariable String storeId) {
        Optional<MongoStore> store = mongoStoreService.getStoreById(storeId);
        return store.map(s -> ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, s))
            .orElseGet(() -> ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null));
    }

    /**
     * 모든 상점 정보를 조회합니다.
     * @return ApiResponse<List<MongoStore>> 모든 상점 정보
     */
    @GetMapping
    public ApiResponse<List<MongoStore>> getAllStores() {
        return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, mongoStoreService.getAllStores());
    }

    /**
     * 특정 상점의 모든 메뉴 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return ApiResponse<List<MongoMenu>> 상점의 메뉴 목록
     */
    @GetMapping("/{storeId}/menus")
    public ApiResponse<List<MongoMenu>> getMenusFromStore(@PathVariable String storeId) {
        // 상점 ID가 유효한지 먼저 확인하는 로직을 추가할 수 있습니다.
        // 현재는 서비스에서 바로 메뉴를 가져온다고 가정
        List<MongoMenu> menus = mongoStoreService.getMenusFromStore(storeId);
        return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.MENU_GET_SUCCESS, menus);
    }

    /**
     * 새로운 상점 정보를 생성합니다.
     * @param mongoStore 생성할 상점 정보 (JSON 요청 본문)
     * @return ApiResponse<MongoStore> 생성된 상점 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // HTTP 201 Created 반환
    public ApiResponse<MongoStore> createStore(@RequestBody MongoStore mongoStore) {
        MongoStore createdStore = mongoStoreService.createStore(mongoStore);
        return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_CREATE_SUCCESS, createdStore);
    }

    /**
     * 특정 상점에 새로운 메뉴를 추가합니다.
     * @param storeId 메뉴를 추가할 상점 ID
     * @param mongoMenu 추가할 메뉴 정보 (JSON 요청 본문)
     * @return ApiResponse<MongoMenu> 추가된 메뉴 정보
     */
    @PostMapping("/{storeId}/menus")
    @ResponseStatus(HttpStatus.CREATED) // HTTP 201 Created 반환
    public ApiResponse<MongoMenu> addMenuToStore(@PathVariable String storeId,
        @RequestBody MongoMenu mongoMenu) {
        // 메뉴를 추가하고, 추가된 메뉴 객체를 반환하거나, 업데이트된 상점 객체를 반환할 수 있습니다.
        // 여기서는 추가된 메뉴 객체를 반환하도록 가정합니다.
        MongoMenu addedMenu = mongoStoreService.addMenuToStore(storeId, mongoMenu);
        return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.MENU_ADD_SUCCESS, addedMenu);
    }
}