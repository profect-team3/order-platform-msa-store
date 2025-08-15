package app.domain.batch;

import app.domain.batch.model.dto.StoreMenuDto;
import app.domain.mongo.model.entity.MenuCollection;
import app.domain.mongo.model.entity.StoreCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreProcessor implements ItemProcessor<StoreMenuDto, StoreCollection> {

	private final ObjectMapper objectMapper;

	@Override
	public StoreCollection process(StoreMenuDto dto) throws Exception {
		StoreCollection storeCollection = new StoreCollection();

		storeCollection.setId(dto.getStoreId().toString());
		storeCollection.setUserId(dto.getUserId());
		storeCollection.setStoreName(dto.getStoreName());
		storeCollection.setDescription(dto.getDescription());
		storeCollection.setCategoryKeys(List.of(dto.getCategoryName()));
		storeCollection.setPrimaryCategory(dto.getCategoryName());
		storeCollection.setAvgRating(dto.getAvgRating());
		storeCollection.setPhoneNumber(dto.getPhoneNumber());
		storeCollection.setMinOrderAmount(dto.getMinOrderAmount());
		storeCollection.setAddress(dto.getAddress());
		storeCollection.setRegionName(dto.getRegionName());
		storeCollection.setRegionFullName(dto.getRegionFullName());
		storeCollection.setStoreAcceptStatus(dto.getStoreAcceptStatus());
		storeCollection.setCreatedAt(Date.from(dto.getCreatedAt().toInstant()));
		storeCollection.setUpdatedAt(Date.from(dto.getUpdatedAt().toInstant()));

		if (dto.getMenuJson() != null) {
			List<MenuCollection> menus = objectMapper.readValue(dto.getMenuJson(),
				objectMapper.getTypeFactory().constructCollectionType(List.class, MenuCollection.class));
			storeCollection.setMenus(menus);
		} else {
			storeCollection.setMenus(new ArrayList<>());
		}

		return storeCollection;
	}
}