package app.domain.store.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.store.service.RegionService;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store/manager/region")
@Tag(name = "지역 추가 API", description = "관리자의 사용자 관리 API")
public class RegionController {

	private final RegionService regionService;

	@PostMapping("/code/{regionId}")
	public ApiResponse<UUID> getRegionIdByCode(@PathVariable("regionId") String regionCode) {
		if (regionCode == null || regionCode.isBlank()) {
			throw new GeneralException(StoreErrorCode.REGIONCODE_NOT_FOUND);
		}

		UUID regionId = regionService.getRegionIdByCode(regionCode);

		return ApiResponse.onSuccess(StoreSuccessStatus._OK, regionId);
	}
}