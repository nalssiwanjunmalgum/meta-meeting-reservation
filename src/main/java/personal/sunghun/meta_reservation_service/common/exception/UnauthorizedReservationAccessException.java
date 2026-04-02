package personal.sunghun.meta_reservation_service.common.exception;

public class UnauthorizedReservationAccessException extends BusinessException {

    public UnauthorizedReservationAccessException() {
        super(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
    }
}
