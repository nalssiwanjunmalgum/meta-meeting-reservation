package personal.sunghun.meta_reservation_service.reservation.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationCancelResponse {
    private final Long reservationId;
    private final String status;
}
