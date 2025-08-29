package app.domain.store.model;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.PagedResponse;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.store.client.ReviewClient;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.entity.QStore;
import app.domain.store.model.entity.Store;
import app.domain.store.status.StoreAcceptStatus;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Repository
public class StoreQueryRepositoryImpl implements StoreQueryRepository {

	private final JPAQueryFactory queryFactory;
	private final ReviewClient reviewClient;


	@Transactional(readOnly = true)
	public PagedResponse<GetStoreListResponse> searchStores(
		String keyword,
		String categoryKeyword,
		StoreAcceptStatus status,
		Pageable pageable
	) {
		QStore store = QStore.store;

		var where = new BooleanBuilder()
			.and(store.deletedAt.isNull())
			.and(status != null ? store.storeAcceptStatus.eq(status) : null)
			// 키워드만 있으면: 가게명 contains
			.and(hasText(keyword) ? store.storeName.containsIgnoreCase(keyword) : null)
			// 카테고리만 있으면: 카테고리명 contains
			.and(hasText(categoryKeyword)
				? store.category.categoryName.isNotNull()
				.and(store.category.categoryName.containsIgnoreCase(categoryKeyword))
				: null);


		var orders = new OrderSpecifier<?>[]{ store.storeName.asc() };

		List<Store> stores = queryFactory.selectFrom(store)
			.where(where)
			.orderBy(orders)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory.select(store.count())
			.from(store)
			.where(where)
			.fetchOne();

		// 리뷰 평균 병합
		List<UUID> ids = stores.stream().map(Store::getStoreId).toList();

		var storeReviewResponse = reviewClient.getStoreReviewAverage(ids);
		if (!storeReviewResponse.isSuccess())
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap  = storeReviewResponse.result().stream()
			.collect(Collectors.toMap(
				ReviewClient.StoreReviewResponse::getStoreId,
				review -> review
			));


		List<GetStoreListResponse> content = stores.stream()
			.map(s -> {
				ReviewClient.StoreReviewResponse review = reviewMap.get(s.getStoreId());
				long number = (review != null) ? review.getNumber() : 0L;
				double avg = (review != null) ? review.getAverage() : 0.0;
				return GetStoreListResponse.from(s, number, avg);
			})
			.toList();

		return PagedResponse.from(
			new PageImpl<>(content, pageable, total == null ? 0 : total)
		);
	}

	private boolean hasText(String s) {
		return s != null && !s.isBlank();
	}


	@Override
	public PagedResponse<GetStoreListResponse> getApprovedStore(Pageable pageable) {
		QStore store = QStore.store;

		List<GetStoreListResponse> stores = queryFactory
			.select(Projections.constructor(GetStoreListResponse.class,
				store.storeId,
				store.storeName,
				store.address,
				store.minOrderAmount,
				Expressions.constant(0.0)
			))
			.from(store)
			.where(store.storeAcceptStatus.eq(StoreAcceptStatus.APPROVE)
				.and(store.deletedAt.isNull()))
			.groupBy(store.storeId)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 전체 개수 쿼리 (페이징용)
		Long total = queryFactory
			.select(store.countDistinct())
			.from(store)
			.where(store.storeAcceptStatus.eq(StoreAcceptStatus.APPROVE)
				.and(store.deletedAt.isNull()))
			.fetchOne();

		// 3. storeId 목록 추출
		List<UUID> storeIds = stores.stream()
			.map(GetStoreListResponse::getStoreId)
			.toList();

		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse=reviewClient.getStoreReviewAverage(storeIds);
		if(!storeReviewResponse.isSuccess()){
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap = storeReviewResponse.result().stream()
			.collect(Collectors.toMap(
				ReviewClient.StoreReviewResponse::getStoreId,
				review -> review
			));
		stores.forEach(s -> s.setAverageRating(reviewMap.get(s.getStoreId()).getAverage()));
		Page<GetStoreListResponse> page = new PageImpl<>(stores, pageable, total != null ? total : 0);

		return PagedResponse.from(page);
	}

	@Override
	public PagedResponse<GetStoreListResponse> getAllStore(StoreAcceptStatus status, Pageable pageable) {
		QStore store = QStore.store;

		List<GetStoreListResponse> stores = queryFactory
			.select(Projections.constructor(
				GetStoreListResponse.class,
				store.storeId,
				store.storeName,
				store.address,
				store.minOrderAmount,
				Expressions.constant(0.0)
			))
			.from(store)
			.where(
				store.storeAcceptStatus.eq(status),
				store.deletedAt.isNull()
			)
			.groupBy(store.storeId)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(store.count())
			.from(store)
			.where(
				store.storeAcceptStatus.eq(status),
				store.deletedAt.isNull()
			)
			.fetchOne();

		List<UUID> storeIds = stores.stream()
			.map(GetStoreListResponse::getStoreId)
			.toList();

		ApiResponse<List<ReviewClient.StoreReviewResponse>> storeReviewResponse=reviewClient.getStoreReviewAverage(storeIds);
		if(!storeReviewResponse.isSuccess()){
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap = storeReviewResponse.result().stream()
			.collect(Collectors.toMap(
				ReviewClient.StoreReviewResponse::getStoreId,
				review -> review
			));
		stores.forEach(s -> s.setAverageRating(reviewMap.get(s.getStoreId()).getAverage()));
		Page<GetStoreListResponse> page = new PageImpl<>(stores, pageable, total != null ? total : 0);

		return PagedResponse.from(page);
	}


}