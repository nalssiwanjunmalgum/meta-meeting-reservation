package personal.sunghun.meta_reservation_service.common.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
