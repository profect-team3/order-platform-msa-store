package app.domain.batch.repository;

import app.domain.batch.dto.BulkDto;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.entity.QCategory;
import app.domain.menu.model.entity.QMenu;
import app.domain.store.model.entity.QRegion;
import app.domain.store.model.entity.QReview;
import app.domain.store.model.entity.QStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BulkRepositoryImpl implements BulkRepository {

    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Override
    public List<BulkDto> findStoresWithDetailsCursor(UUID lastStoreId, int limit) {
        QStore s = QStore.store;
        QRegion region = QRegion.region;
        QCategory category = QCategory.category;
        QReview review = QReview.review;
        QCategory parentCategory = new QCategory("parentCategory");
        QCategory grandparentCategory = new QCategory("grandparentCategory");

        com.querydsl.core.types.Expression<Double> avgRatingExpr = JPAExpressions.select(review.rating.avg().coalesce(0.0))
                .from(review)
                .where(review.store.eq(s.storeId).and(review.deletedAt.isNull()));

        com.querydsl.core.types.Expression<Long> reviewCountExpr = JPAExpressions.select(review.count())
                .from(review)
                .where(review.store.eq(s.storeId).and(review.deletedAt.isNull()));

        List<com.querydsl.core.Tuple> tuples = new JPAQuery<>(entityManager)
                .select(
                        s.storeId,
                        s.userId,
                        s.storeName,
                        s.description,
                        s.minOrderAmount,
                        s.address,
                        s.phoneNumber,
                        s.storeAcceptStatus,
                        s.createdAt,
                        s.updatedAt,
                        s.deletedAt,
                        region.regionName,
                        region.fullName,
                        category.categoryName,
                        parentCategory.categoryName,
                        grandparentCategory.categoryName,
                        avgRatingExpr,
                        reviewCountExpr
                )
                .from(s)
                .join(s.region, region)
                .join(s.category, category)
                .leftJoin(category.parentCategory, parentCategory)
                .leftJoin(parentCategory.parentCategory, grandparentCategory)
                .where(s.deletedAt.isNull())
                .where(lastStoreId == null ? Expressions.TRUE : s.storeId.gt(lastStoreId))
                .orderBy(s.storeId.asc())
                .limit(limit)
                .fetch();

        List<BulkDto> stores = tuples.stream().map(tuple -> {
            BulkDto dto = new BulkDto();
            dto.setStoreKey(tuple.get(s.storeId));
            dto.setUserId(tuple.get(s.userId));
            dto.setStoreName(tuple.get(s.storeName));
            dto.setDescription(tuple.get(s.description));
            dto.setMinOrderAmount(tuple.get(s.minOrderAmount));
            dto.setAddress(tuple.get(s.address));
            dto.setPhoneNumber(tuple.get(s.phoneNumber));
            dto.setStoreAcceptStatus(tuple.get(s.storeAcceptStatus).toString());
            dto.setCreatedAt(java.sql.Timestamp.valueOf(tuple.get(s.createdAt)));
            dto.setUpdatedAt(java.sql.Timestamp.valueOf(tuple.get(s.updatedAt)));
            if (tuple.get(s.deletedAt) != null) {
                dto.setDeletedAt(java.sql.Timestamp.valueOf(tuple.get(s.deletedAt)));
            }
            dto.setRegionName(tuple.get(region.regionName));
            dto.setRegionFullName(tuple.get(region.fullName));
            String categoryName = tuple.get(category.categoryName);
            String parentCategoryName = tuple.get(parentCategory.categoryName);
            String grandparentCategoryName = tuple.get(grandparentCategory.categoryName);

            List<String> categoryKeys = new java.util.ArrayList<>();
            if (grandparentCategoryName != null) categoryKeys.add(grandparentCategoryName);
            if (parentCategoryName != null) categoryKeys.add(parentCategoryName);
            categoryKeys.add(categoryName);
            dto.setCategoryKeys(categoryKeys);

            dto.setAvgRating(tuple.get(avgRatingExpr));
            dto.setReviewCount(tuple.get(reviewCountExpr));
            return dto;
        }).collect(Collectors.toList());

        if (stores.isEmpty()) {
            return List.of();
        }

        List<UUID> storeIds = stores.stream().map(BulkDto::getStoreKey).toList();
        QMenu menu = QMenu.menu;
        List<Menu> menus = new JPAQuery<>(entityManager)
                .select(menu)
                .from(menu)
                .where(menu.store.storeId.in(storeIds).and(menu.deletedAt.isNull()))
                .fetch();

        Map<UUID, List<Menu>> menusByStoreId = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getStore().getStoreId()));

        return stores.stream()
                .map(dto -> {
                    List<Menu> storeMenus = menusByStoreId.getOrDefault(dto.getStoreKey(), List.of());
                    try {
                        String menuJson = objectMapper.writeValueAsString(
                                storeMenus.stream()
                                        .map(m -> Map.of(
                                                "menuId", m.getMenuId().toString(),
                                                "name", m.getName(),
                                                "price", m.getPrice(),
                                                "description", m.getDescription(),
                                                "isHidden", m.isHidden()
                                        ))
                                        .toList()
                        );
                        dto.setMenuJson(menuJson);
                    } catch (Exception e) {
                        dto.setMenuJson("[]");
                    }
                    return dto;
                })
                .toList();
    }
}