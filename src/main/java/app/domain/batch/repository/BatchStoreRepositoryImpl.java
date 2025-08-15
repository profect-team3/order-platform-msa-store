package app.domain.batch.repository;

import app.domain.batch.dto.StoreMenuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BatchStoreRepositoryImpl implements BatchStoreRepository {

    private final EntityManager entityManager;

    @Override
    public List<StoreMenuDto> findStoresWithDetailsCursor(UUID lastStoreId, int limit) {
        String sql = """
            SELECT s.store_id, s.user_id, s.store_name, s.description, s.min_order_amount, 
                   s.address, s.phone_number, s.store_accept_status, s.created_at, s.updated_at, s.deleted_at,
                   r.region_name, r.full_name as region_full_name, c.category_name,
                   (SELECT COALESCE(AVG(rv.rating), 0.0) 
                    FROM p_review rv 
                    WHERE rv.store_id = s.store_id AND rv.deleted_at IS NULL) AS avg_rating,
                   (SELECT COUNT(*) FROM p_review rv WHERE rv.store_id = s.store_id AND rv.deleted_at IS NULL) AS review_count,
                   (SELECT COALESCE(
                       json_agg(json_build_object(
                           'menuId', m.menu_id, 
                           'name', m.name, 
                           'price', m.price, 
                           'description', m.description, 
                           'isHidden', m.is_hidden
                       )),
                       '[]'::json
                   )
                    FROM p_menu m 
                    WHERE m.store_id = s.store_id AND m.deleted_at IS NULL) AS menu_json
            FROM p_store s 
            JOIN p_region r ON s.region_id = r.region_id 
            JOIN p_category c ON s.category_id = c.category_id 
            WHERE (CAST(:lastStoreId AS uuid) IS NULL OR s.store_id > CAST(:lastStoreId AS uuid))
            ORDER BY s.store_id
            LIMIT :limit""";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("lastStoreId", lastStoreId);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream()
            .map(this::mapToStoreMenuDto)
            .toList();
    }

    private StoreMenuDto mapToStoreMenuDto(Object[] row) {
        StoreMenuDto dto = new StoreMenuDto();
        dto.setStoreId((UUID) row[0]);
        dto.setUserId(((Number) row[1]).longValue());
        dto.setStoreName((String) row[2]);
        dto.setDescription((String) row[3]);
        dto.setMinOrderAmount(((Number) row[4]).longValue());
        dto.setAddress((String) row[5]);
        dto.setPhoneNumber((String) row[6]);
        dto.setStoreAcceptStatus((String) row[7]);
        dto.setCreatedAt((Timestamp) row[8]);
        dto.setUpdatedAt((Timestamp) row[9]);
        dto.setDeletedAt((Timestamp) row[10]);
        dto.setRegionName((String) row[11]);
        dto.setRegionFullName((String) row[12]);
        dto.setCategoryName((String) row[13]);
        dto.setAvgRating(((Number) row[14]).doubleValue());
        dto.setReviewCount(((Number) row[15]).longValue());
        dto.setMenuJson((String) row[16]);
        return dto;
    }
}