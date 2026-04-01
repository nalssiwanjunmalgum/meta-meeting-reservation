package personal.sunghun.meta_reservation_service.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationCreateResponse {
    private final Long reservationId;
    private final Long resourceId;
    private final Long userId;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String status;
}
