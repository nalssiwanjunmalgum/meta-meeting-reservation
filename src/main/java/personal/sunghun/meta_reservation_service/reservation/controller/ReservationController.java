package personal.sunghun.meta_reservation_service.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import personal.sunghun.meta_reservation_service.reservation.domain.Reservation;
import personal.sunghun.meta_reservation_service.reservation.dto.request.ReservationCreateRequest;
import personal.sunghun.meta_reservation_service.reservation.dto.response.ReservationCancelResponse;
import personal.sunghun.meta_reservation_service.reservation.dto.response.ReservationCreateResponse;
import personal.sunghun.meta_reservation_service.reservation.dto.response.ReservationSummaryResponse;
import personal.sunghun.meta_reservation_service.reservation.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @RequestParam Long userId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.createReservation(
                userId,
                request.getResourceId(),
                request.getStartAt(),
                request.getEndAt()
        );

        ReservationCreateResponse response = toCreateResponse(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReservationSummaryResponse>> getMyReservations(
            @RequestParam Long userId
    ) {
        List<Reservation> reservations = reservationService.getMyReservations(userId);

        List<ReservationSummaryResponse> response = reservations.stream()
                .map(this::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationCancelResponse> cancelReservation(
            @RequestParam Long userId,
            @PathVariable Long reservationId
    ) {
        Reservation canceledReservation = reservationService.cancelReservation(userId, reservationId);

        ReservationCancelResponse response = toCancelResponse(canceledReservation);
        return ResponseEntity.ok(response);
    }

    private ReservationCreateResponse toCreateResponse(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getUser().getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus().name()
        );
    }

    private ReservationSummaryResponse toSummaryResponse(Reservation reservation) {
        return new ReservationSummaryResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus().name()
        );
    }

    private ReservationCancelResponse toCancelResponse(Reservation reservation) {
        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getStatus().name()
        );
    }
}
