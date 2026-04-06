package personal.sunghun.meta_reservation_service.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import personal.sunghun.meta_reservation_service.reservation.domain.Reservation;
import personal.sunghun.meta_reservation_service.reservation.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser_IdOrderByStartTimeAsc(Long userId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from Reservation r
            where r.resource.id = :resourceId
              and r.status <> :canceledStatus
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            Long resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus canceledStatus
    );
}
