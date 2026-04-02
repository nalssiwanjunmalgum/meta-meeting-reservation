package personal.sunghun.meta_reservation_service.common.exception;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException() {
        super(ErrorCode.RESERVATION_NOT_FOUND);
    }
}
