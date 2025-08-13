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
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.domain.store.client.ReviewClient;
import app.domain.store.model.dto.response.GetStoreListResponse;
import app.domain.store.model.entity.QStore;
import app.domain.store.model.entity.Store;
import app.domain.store.status.StoreAcceptStatus;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
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
		Map<UUID, ReviewClient.StoreReviewResponse> reviewMap = Map.of();
		
		if (!ids.isEmpty()) {
			try {
				var storeReviewResponse = reviewClient.getStoreReviewAverage(ids);
				if (storeReviewResponse.isSuccess()) {
					reviewMap = storeReviewResponse.result().stream()
						.collect(Collectors.toMap(
							ReviewClient.StoreReviewResponse::getStoreId,
							review -> review
						));
				}
			} catch (Exception e) {
				throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
			}
		}

		Map<UUID, ReviewClient.StoreReviewResponse> finalReviewMap = reviewMap;
		List<GetStoreListResponse> content = stores.stream()
			.map(s -> {
				ReviewClient.StoreReviewResponse review = finalReviewMap.get(s.getStoreId());
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



}