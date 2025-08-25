package app.domain.store.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StoreSuccessStatus implements BaseCode {
	// 일반적인 응답
	_OK(HttpStatus.OK, "COMMON200", "성공입니다."),

	// 가게 관련 응답
	STORE_CREATED_SUCCESS(HttpStatus.CREATED, "STORE201", "가게가 성공적으로 생성되었습니다."),
	STORE_UPDATED_SUCCESS(HttpStatus.OK, "STORE202", "가게 정보가 성공적으로 업데이트되었습니다."),
	STORE_DELETED_SUCCESS(HttpStatus.OK, "STORE203", "가게가 성공적으로 삭제되었습니다."),
	STORE_EXISTS(HttpStatus.OK,"STORE204","가게가 존재합니다."),
	STORE_OWNER_SUCCESS(HttpStatus.OK,"STORE205","가게의 점주가 맞습니다"),
	STORE_NAME_FETCHED(HttpStatus.OK,"STORE206","해당 가게 이름 조회에 성공했습니다"),

	// 주문 관련 응답
	ORDER_ACCEPTED_SUCCESS(HttpStatus.OK, "STORE207", "주문이 성공적으로 수락되었습니다."),
	ORDER_REJECTED_SUCCESS(HttpStatus.OK, "STORE208", "주문이 성공적으로 거절되었습니다."),

	// 가게관련 유저 응답
	CUSTOMER_GET_STORE_LIST_OK(HttpStatus.OK, "CUSTOMER200", "사용자의 가게 목록 조회가 성공했습니다."),
	CUSTOMER_GET_STORE_DETAIL_OK(HttpStatus.OK, "CUSTOMER201", "사용자의 가게 상세 조회가 성공했습니다."),
	CUSTOMER_SEARCH_STORE_OK(HttpStatus.OK, "CUSTOMER202", "사용자의 가게 검색이 성공했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(true)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(true)
			.httpStatus(httpStatus)
			.build();
	}
}
