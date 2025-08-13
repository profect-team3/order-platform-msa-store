package app.domain.mongo.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MongoStoreMenuErrorCode implements BaseCode {

	MONGO_DB_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MONGO501", "MongoDB와의 연결이 불안정합니다."),
	INVALID_MONGO_QUERY(HttpStatus.BAD_REQUEST, "MONGO400", "잘못된 형식의 데이터가 포함되어 쿼리 실행에 실패했습니다. 요청 데이터를 확인하세요."),
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404", "요청하신 가게를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}