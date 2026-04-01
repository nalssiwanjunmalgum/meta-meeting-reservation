package personal.sunghun.meta_reservation_service.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationSummaryResponse {
    private final Long reservationId;
    private final Long resourceId;
    private final String resourceName;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String status;
}
