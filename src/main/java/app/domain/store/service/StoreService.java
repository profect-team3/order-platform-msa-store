package app.domain.store.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import app.domain.store.client.OrderClient;
import app.domain.store.client.ReviewClient;
import app.domain.store.client.UserClient;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.store.model.dto.response.GetReviewResponse;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.OrderInfo;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.dto.response.StoreOrderInfo;
import app.domain.store.model.dto.response.StoreOrderListResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;
	private final CategoryRepository categoryRepository;
	private final MenuRepository menuRepository;
	private final OrderClient orderClient;
	private final ReviewClient reviewClient;
	private final UserClient userClient;

	@Transactional
	public StoreApproveResponse createStore(StoreApproveRequest request, Long userId) {

		try {
			ApiResponse<Boolean> isUserExistsResponse = userClient.isUserExists();

			Boolean isUserExists=isUserExistsResponse.result();

			if (!isUserExists) {
				throw new GeneralException(StoreErrorCode.USER_NOT_FOUND);
			}
		} catch (HttpClientErrorException|HttpServerErrorException e){
			log.error("User Service Error: {}",e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}

		Region region = regionRepository.findById(request.getRegionId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.REGION_NOT_FOUND));

		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.CATEGORY_NOT_FOUND));

		Store store = new Store(null, userId, region, category, request.getStoreName(), request.getDesc(),
			request.getAddress(), request.getPhoneNumber(), request.getMinOrderAmount(), StoreAcceptStatus.PENDING);

		Store savedStore = storeRepository.save(store);

		return new StoreApproveResponse(savedStore.getStoreId(), savedStore.getStoreAcceptStatus().name());
	}

	@Transactional
	public StoreInfoUpdateResponse updateStoreInfo(StoreInfoUpdateRequest request, Long userId) {

		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		if (request.getCategoryId() != null) {
			Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new GeneralException(StoreErrorCode.CATEGORY_NOT_FOUND));
			store.setCategory(category);
		}
		if (request.getName() != null) {
			store.setStoreName(request.getName());
		}
		if (request.getAddress() != null) {
			store.setAddress(request.getAddress());
		}
		if (request.getPhoneNumber() != null) {
			store.setPhoneNumber(request.getPhoneNumber());
		}
		if (request.getMinOrderAmount() != null) {
			store.setMinOrderAmount(request.getMinOrderAmount());
		}
		if (request.getDesc() != null) {
			store.setDescription(request.getDesc());
		}

		Store updatedStore = storeRepository.save(store);
		return new StoreInfoUpdateResponse(updatedStore.getStoreId());
	}

	@Transactional
	public void deleteStore(UUID storeId, Long userId) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		store.markAsDeleted();
	}

	@Transactional(readOnly = true)
	public MenuListResponse getStoreMenuList(UUID storeId, Long userId) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		List<Menu> menus = menuRepository.findByStoreAndDeletedAtIsNull(store);

		List<MenuListResponse.MenuDetail> menuDetails = menus.stream()
			.map(menu -> new MenuListResponse.MenuDetail(menu.getMenuId(), menu.getName(), menu.getPrice(),
				menu.getDescription(), menu.isHidden()))
			.collect(Collectors.toList());

		return new MenuListResponse(store.getStoreId(), menuDetails);
	}

	@Transactional(readOnly = true)
	public List<GetReviewResponse> getStoreReviewList(UUID storeId, Long userId) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		try {
			ApiResponse<List<GetReviewResponse>> getReviewResponse = reviewClient.getReviewsByStoreId(storeId);
			return getReviewResponse.result();
		} catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("Review Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}

	}

	@Transactional(readOnly = true)
	public StoreOrderListResponse getStoreOrderList(UUID storeId, Long userId) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}
		ApiResponse<List<StoreOrderInfo>> storeOrderInfoResponse;
		try{
			storeOrderInfoResponse= orderClient.getOrdersByStoreId(storeId);
		} catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus.ORDER_NOT_FOUND);
		}

		List<StoreOrderInfo> orders= storeOrderInfoResponse.result();
		List<StoreOrderListResponse.StoreOrderDetail> orderDetails = orders.stream()
			.map(orderInfo -> {
				ApiResponse<String> getUserNameResponse;
				try{
					getUserNameResponse =userClient.getUserName();
				} catch (HttpClientErrorException | HttpServerErrorException e){
					log.error("User Service Error: {}", e.getResponseBodyAsString());
					throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
				}

				String customerName = getUserNameResponse.result();
				return new StoreOrderListResponse.StoreOrderDetail(
					orderInfo.getOrderId(),
					orderInfo.getCustomerId(),
					customerName,
					orderInfo.getTotalPrice(),
					orderInfo.getOrderStatus(),
					orderInfo.getOrderedAt()
				);
			})
			.collect(Collectors.toList());

		return new StoreOrderListResponse(store.getStoreId(), orderDetails);
	}

	@Transactional
	public void acceptOrder(UUID orderId, Long userId) {
		ApiResponse<OrderInfo> orderInfoResponse;
		try {
			orderInfoResponse = orderClient.getOrderInfo(orderId);
		} catch (HttpClientErrorException|HttpServerErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus.ORDER_NOT_FOUND);
		}
		OrderInfo orderInfo=orderInfoResponse.result();

		Store store = storeRepository.findById(orderInfo.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.NOT_STORE_OWNER);
		}
		ApiResponse<String> updateOrderStatusResponse;
		try{
			updateOrderStatusResponse=orderClient.updateOrderStatus(orderId, "ACCEPTED");
		} catch (HttpClientErrorException|HttpServerErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void rejectOrder(UUID orderId, Long userId) {
		ApiResponse<OrderInfo> orderInfoResponse;
		try {
			orderInfoResponse = orderClient.getOrderInfo(orderId);
		} catch (HttpClientErrorException|HttpServerErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus.ORDER_NOT_FOUND);
		}
		OrderInfo orderInfo=orderInfoResponse.result();

		Store store = storeRepository.findById(orderInfo.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.NOT_STORE_OWNER);
		}
		ApiResponse<String> updateOrderStatusResponse;
		try{
			updateOrderStatusResponse=orderClient.updateOrderStatus(orderId, "REJECTED");
		} catch (HttpClientErrorException|HttpServerErrorException e){
			log.error("Order Service Error: {}", e.getResponseBodyAsString());
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}
