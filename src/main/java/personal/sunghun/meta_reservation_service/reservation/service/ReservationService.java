package personal.sunghun.meta_reservation_service.reservation.service;


import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.sunghun.meta_reservation_service.common.exception.AlreadyCanceledReservationException;
import personal.sunghun.meta_reservation_service.common.exception.InvalidReservationTimeException;
import personal.sunghun.meta_reservation_service.common.exception.ReservationCannotBeCanceledException;
import personal.sunghun.meta_reservation_service.common.exception.ReservationConflictException;
import personal.sunghun.meta_reservation_service.common.exception.ReservationNotFoundException;
import personal.sunghun.meta_reservation_service.common.exception.ResourceNotFoundException;
import personal.sunghun.meta_reservation_service.common.exception.UnauthorizedReservationAccessException;
import personal.sunghun.meta_reservation_service.common.exception.UserNotFoundException;
import personal.sunghun.meta_reservation_service.reservation.domain.Reservation;
import personal.sunghun.meta_reservation_service.reservation.domain.ReservationStatus;
import personal.sunghun.meta_reservation_service.reservation.repository.ReservationRepository;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.resource.repository.ResourceRepository;
import personal.sunghun.meta_reservation_service.user.domain.User;
import personal.sunghun.meta_reservation_service.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            ReservationRepository reservationRepository
    ) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation createReservation(
            Long userId,
            Long resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        User user = getUserOrThrow(userId);
        Resource resource = getResourceOrThrow(resourceId);

        validateReservationTime(startTime, endTime);
        validateNoOverlappingReservation(resourceId, startTime, endTime);

        Reservation reservation = new Reservation(
                user,
                resource,
                startTime,
                endTime,
                ReservationStatus.RESERVED
        );

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getMyReservations(Long userId) {
        getUserOrThrow(userId);
        return reservationRepository.findByUser_IdOrderByStartTimeAsc(userId);
    }

    @Transactional
    public Reservation cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        validateReservationOwner(reservation, userId);
        validateNotAlreadyCanceled(reservation);
        validateCancelable(reservation);

        reservation.cancel();
        return reservation;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private Resource getResourceOrThrow(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private void validateReservationTime(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new InvalidReservationTimeException();
        }
    }

    private void validateNoOverlappingReservation(
            Long resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        boolean exists = reservationRepository.existsOverlappingReservation(
                resourceId,
                startTime,
                endTime,
                ReservationStatus.CANCELED
        );

        if (exists) {
            throw new ReservationConflictException();
        }
    }

    private void validateReservationOwner(Reservation reservation, Long userId) {
        if (!reservation.isOwnedBy(userId)) {
            throw new UnauthorizedReservationAccessException();
        }
    }

    private void validateNotAlreadyCanceled(Reservation reservation) {
        if (reservation.isCanceled()) {
            throw new AlreadyCanceledReservationException();
        }
    }

    private void validateCancelable(Reservation reservation) {
        if (!LocalDateTime.now().isBefore(reservation.getStartTime())) {
            throw new ReservationCannotBeCanceledException();
        }
    }
}
