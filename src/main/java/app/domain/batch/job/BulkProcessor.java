package app.domain.batch.job;

import app.domain.batch.dto.BulkDto;
import app.domain.mongo.model.entity.MenuCollection;
import app.domain.mongo.model.entity.StoreCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BulkProcessor implements ItemProcessor<BulkDto, StoreCollection> {

	private final ObjectMapper objectMapper;

	@Override
	public StoreCollection process(BulkDto dto) throws Exception {
		StoreCollection storeCollection = new StoreCollection();

		storeCollection.setStoreKey(dto.getStoreKey().toString());
		storeCollection.setUserId(dto.getUserId());
		storeCollection.setStoreName(dto.getStoreName());
		storeCollection.setDescription(dto.getDescription());
		storeCollection.setCategoryKeys(dto.getCategoryKeys());
		storeCollection.setReviewCount(dto.getReviewCount());
		storeCollection.setAvgRating(dto.getAvgRating());
		storeCollection.setPhoneNumber(dto.getPhoneNumber());
		storeCollection.setMinOrderAmount(dto.getMinOrderAmount());
		storeCollection.setAddress(dto.getAddress());
		storeCollection.setRegionName(dto.getRegionName());
		storeCollection.setRegionFullName(dto.getRegionFullName());
		storeCollection.setStoreAcceptStatus(dto.getStoreAcceptStatus());
		storeCollection.setIsActive(dto.getStoreAcceptStatus().equals("APPROVE"));
		storeCollection.setCreatedAt(Date.from(dto.getCreatedAt().toInstant()));
		storeCollection.setUpdatedAt(Date.from(dto.getUpdatedAt().toInstant()));

		List<MenuCollection> menuCollections = new ArrayList<>();
		if (dto.getMenuJson() != null && !dto.getMenuJson().trim().isEmpty() && !dto.getMenuJson().equals("[]")) {
			try {
				List<Map<String, Object>> menuMaps = objectMapper.readValue(
					dto.getMenuJson(), 
					new TypeReference<List<Map<String, Object>>>() {}
				);
				
				menuCollections = menuMaps.stream()
					.map(menuMap -> {
						MenuCollection menuCollection = new MenuCollection();
						menuCollection.setMenuId((String) menuMap.get("menuId"));
						menuCollection.setName((String) menuMap.get("name"));
						menuCollection.setPrice(((Number) menuMap.get("price")).intValue());
						menuCollection.setDescription((String) menuMap.get("description"));
						menuCollection.setHidden((Boolean) menuMap.get("isHidden"));
						return menuCollection;
					})
					.toList();
			} catch (Exception e) {
				menuCollections = new ArrayList<>();
			}
		}
		
		storeCollection.setMenus(menuCollections);

		return storeCollection;
	}
}
