package personal.sunghun.meta_reservation_service.common.exception;

public class InvalidReservationTimeException extends BusinessException {

    public InvalidReservationTimeException() {
        super(ErrorCode.INVALID_RESERVATION_TIME);
    }
}
