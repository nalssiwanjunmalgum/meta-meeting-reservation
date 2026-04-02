package personal.sunghun.meta_reservation_service.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "회의실을 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다."),

    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_TIME", "예약 시작 시간은 종료 시간보다 빨라야 합니다."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "RESERVATION_CONFLICT", "이미 해당 시간대에 예약이 존재합니다."),
    UNAUTHORIZED_RESERVATION_ACCESS(HttpStatus.FORBIDDEN, "UNAUTHORIZED_RESERVATION_ACCESS", "본인 예약만 처리할 수 있습니다."),
    ALREADY_CANCELED_RESERVATION(HttpStatus.BAD_REQUEST, "ALREADY_CANCELED_RESERVATION", "이미 취소된 예약입니다."),
    RESERVATION_CANNOT_BE_CANCELED(HttpStatus.BAD_REQUEST, "RESERVATION_CANNOT_BE_CANCELED", "예약 시작 시간이 지난 예약은 취소할 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
