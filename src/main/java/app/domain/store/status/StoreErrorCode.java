package app.domain.store.status;

import org.springframework.http.HttpStatus;

import app.commonUtil.apiPayload.code.BaseCode;
import app.commonUtil.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseCode {

	STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE001", "존재하지 않는 매장 카테고리입니다."),
	REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE002", "존재하지 않는 지역입니다."),
	MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE003", "존재하지 않는 가맹점입니다."),
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE004", "해당 가맹점을 찾을 수 없습니다."),
	REGIONCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE005", "지역코드가 존재하지 않습니다."),

	REGION_ID_NULL(HttpStatus.BAD_REQUEST, "STORE006", "regionId는 null일 수 없습니다."),
	CATEGORY_ID_NULL(HttpStatus.BAD_REQUEST, "STORE007", "카테고리는 null일 수 없습니다."),
	ADDRESS_NULL(HttpStatus.BAD_REQUEST, "STORE008", "주소는 null일 수 없습니다."),
	STORE_NAME_NULL(HttpStatus.BAD_REQUEST, "STORE009", "가게 이름은 null일 수 없습니다."),
	MIN_ORDER_AMOUNT_NULL(HttpStatus.BAD_REQUEST, "STORE010", "최소 주문 금액은 null일 수 없습니다."),
	MIN_ORDER_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "STORE011", "최소 주문 금액 오류"),
	DUPLICATE_STORE_NAME_IN_REGION(HttpStatus.CONFLICT, "STORE012", "지역, 가게명 중복"),
	STORE_ID_NULL(HttpStatus.BAD_REQUEST, "STORE013", "storeId는 null일 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE014", "존재하지 않는 사용자입니다."),
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE015", "존재하지 않는 카테고리입니다."),

	INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "STORE016", "권한이 없습니다."),

	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE017", "해당 주문을 찾을 수 없습니다."),
	NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "STORE018", "해당 가게의 점주가 아닙니다."),
	INVALID_STORE_STATUS(HttpStatus.NOT_FOUND,"STORE019","이미 처리된 상태명 입니다."),
	CART_ITEM_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"STORE020","장바구니 아이템 파싱에 실패했습니다."),
	CART_REDIS_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"STORE021","장바구니 조회에 실패했습니다."),
	CART_NOT_FOUND(HttpStatus.NOT_FOUND,"STORE022","장바구니가 존재하지 않습니다."),
	ORDER_DIFFERENT_STORE(HttpStatus.BAD_REQUEST,"STORE023","장바구니에 두 매장의 메뉴가 담겨있습니다"),
	KAFKA_MESSAGE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "STORE024", "카프카 메시지 파싱에 실패했습니다."),
	INVALID_TOTAL_PRICE(HttpStatus.BAD_REQUEST, "STORE025", "주문 총액이 일치하지 않습니다.");

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
			.isSuccess(false)
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}