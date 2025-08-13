// package app.domain.menu.client;
//
// import java.util.List;
// import java.util.UUID;
//
// import org.springframework.cloud.openfeign.FeignClient;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
//
// import app.domain.menu.model.dto.response.MenuInfoResponse;
//
// @FeignClient(name = "menu-service", url = "${internal.menu-service.url}")
// public interface InternalStoreMenuClient {
//
// 	@PostMapping("/internal/menus/batch")
// 	List<MenuInfoResponse> getMenuInfoList(@RequestBody List<UUID> menuIds);
// }
