package app.domain.mongo.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MongoStoreMenuSuccessCode implements BaseCode {

	MENU_GET_SUCCESS(HttpStatus.OK, "MONGO200", "메뉴 조회 성공"),
	STORE_GET_SUCCESS(HttpStatus.OK, "MONGO200", "가게 조회 성공");

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

