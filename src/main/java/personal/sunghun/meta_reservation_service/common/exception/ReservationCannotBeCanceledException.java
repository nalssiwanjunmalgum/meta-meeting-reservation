package personal.sunghun.meta_reservation_service.common.exception;

public class ReservationCannotBeCanceledException extends BusinessException {

    public ReservationCannotBeCanceledException() {
        super(ErrorCode.RESERVATION_CANNOT_BE_CANCELED);
    }
}
