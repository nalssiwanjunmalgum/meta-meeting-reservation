package personal.sunghun.meta_reservation_service.common.exception;

public class DuplicateUserEmailException extends BusinessException {

    public DuplicateUserEmailException() {
        super(ErrorCode.DUPLICATE_USER_EMAIL);
    }
}
