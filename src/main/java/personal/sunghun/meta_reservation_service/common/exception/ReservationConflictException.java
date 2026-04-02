package personal.sunghun.meta_reservation_service.common.exception;

public class ReservationConflictException extends BusinessException {

    public ReservationConflictException() {
        super(ErrorCode.RESERVATION_CONFLICT);
    }
}
