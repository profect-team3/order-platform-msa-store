package app.domain.mongo;

import app.commonUtil.apiPayload.ApiResponse;
import app.domain.mongo.model.entity.StoreCollection;
import app.domain.mongo.status.MongoStoreMenuErrorCode;
import app.domain.mongo.status.MongoStoreMenuSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mongo/stores")
@Tag(name = "MongoDB Store Query API", description = "MongoDB 기반 가게 검색 API")
public class StoreCollectionQueryController {

    private final StoreCollectionQueryService storeCollectionQueryService;

    @GetMapping("/search")
    @Operation(summary = "통합 검색 API", description = "가게 이름, 카테고리, 메뉴 이름에서 키워드로 활성 상태인 가게를 검색합니다 ")
    @Parameter(name = "keyword", description = "검색할 키워드", required = true)
    public ApiResponse<List<StoreCollection>> searchStores(@RequestParam("keyword") String keyword) {
        List<StoreCollection> stores = storeCollectionQueryService.searchStores(keyword);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }

    @GetMapping("/{storeKey}")
    @Operation(summary = "메뉴 포함 상세 조회 API", description = "가게 ID를 사용하여 모든 메뉴(보임 처리)를 불러옵니다.")
    @Parameter(name = "storeId", description = "조회할 가게의 Key", required = true)
    public ApiResponse<StoreCollection> getStoreByKey(@PathVariable String storeKey) {
        return storeCollectionQueryService.findStoreByStoreKey(storeKey)
            .map(store -> ApiResponse.onSuccess(MongoStoreMenuSuccessCode.MENU_GET_SUCCESS, store))
            .orElse(ApiResponse.onFailure(MongoStoreMenuErrorCode.MENU_NOT_FOUND, null));
    }

    @GetMapping
    @Operation(summary = "전체 가게 목록 조회 API", description = "DB에 저장된 전체 가게 목록을 조회합니다.")
    public ApiResponse<Page<StoreCollection>> getAllStores(@ParameterObject Pageable pageable) {
        Page<StoreCollection> stores = storeCollectionQueryService.findAllStores(pageable);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }

    @GetMapping("/search-by-name")
    @Operation(summary = "가게 이름으로 검색 API", description = "가게 이름에 키워드가 포함된 가게 목록을 조회합니다. (대소문자 무관)")
    @Parameter(name = "keyword", description = "검색할 가게 이름 키워드", required = true)
    public ApiResponse<List<StoreCollection>> searchStoresByName(@RequestParam("keyword") String keyword) {
        List<StoreCollection> stores = storeCollectionQueryService.searchStoresByName(keyword);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }

    @GetMapping("/search-by-category")
    @Operation(summary = "카테고리로 가게 검색 API", description = "제공된 카테고리 키워드를 포함하는 가게 목록을 조회합니다. (대소문자 무관)")
    @Parameter(name = "category", description = "검색할 카테고리 키워드", required = true)
    public ApiResponse<List<StoreCollection>> searchStoresByCategory(@RequestParam("category") String category) {
        List<StoreCollection> stores = storeCollectionQueryService.searchStoresByCategory(category);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }

    @GetMapping("/search-by-menu")
    @Operation(summary = "메뉴 이름으로 가게 검색 API", description = "메뉴 이름에 키워드가 포함된 가게 목록을 조회합니다. (대소문자 무관)")
    @Parameter(name = "keyword", description = "검색할 메뉴 이름 키워드", required = true)
    public ApiResponse<List<StoreCollection>> searchStoresByMenuName(@RequestParam("keyword") String keyword) {
        List<StoreCollection> stores = storeCollectionQueryService.searchStoresByMenuName(keyword);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }

    @GetMapping("/filter-by-menu")
    @Operation(
            summary = "메뉴 이름으로 가게 및 메뉴 필터링 API",
            description = "메뉴 이름에 키워드가 포함된 가게를 찾고, 해당 가게의 메뉴 목록에서는 키워드와 일치하는 메뉴만 필터링하여 반환합니다. (대소문자 무관)"
    )
    @Parameter(name = "keyword", description = "검색 및 필터링할 메뉴 이름 키워드", required = true)
    public ApiResponse<List<StoreCollection>> filterStoresByMenuNameAndReturnFilteredMenus(@RequestParam("keyword") String keyword) {
        List<StoreCollection> stores = storeCollectionQueryService.filterStoresByMenuNameAndReturnFilteredMenus(keyword);
        if (stores.isEmpty()) {
            return ApiResponse.onFailure(MongoStoreMenuErrorCode.STORE_NOT_FOUND, null);
        } else {
            return ApiResponse.onSuccess(MongoStoreMenuSuccessCode.STORE_GET_SUCCESS, stores);
        }
    }
}
