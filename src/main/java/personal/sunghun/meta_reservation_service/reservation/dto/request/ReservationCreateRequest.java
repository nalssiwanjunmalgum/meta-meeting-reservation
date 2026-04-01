package personal.sunghun.meta_reservation_service.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationCreateRequest {

    @NotNull(message = "리소스 ID는 필수입니다.")
    private Long resourceId;

    @NotNull(message = "예약 시작 시간은 필수입니다.")
    @Future(message = "예약 시작 시간은 현재보다 미래여야 합니다.")
    private LocalDateTime startAt;

    @NotNull(message = "예약 종료 시간은 필수입니다.")
    @Future(message = "예약 종료 시간은 현재보다 미래여야 합니다.")
    private LocalDateTime endAt;
}
