package personal.sunghun.meta_reservation_service.common.exception;

public class AlreadyCanceledReservationException extends BusinessException{

    public AlreadyCanceledReservationException() {
        super(ErrorCode.ALREADY_CANCELED_RESERVATION);
    }
}
