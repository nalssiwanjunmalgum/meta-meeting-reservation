package personal.sunghun.meta_reservation_service.common.exception;

public class InvalidLoginException extends BusinessException {

    public InvalidLoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }
}
