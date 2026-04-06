package personal.sunghun.meta_reservation_service.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import personal.sunghun.meta_reservation_service.common.entity.BaseTimeEntity;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.user.domain.User;

@Getter
@Entity
@Table(name = "reservations")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "resource_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Resource resource;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReservationStatus status;

    public Reservation(User user, Resource resource,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.resource = resource;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user != null
                && this.user.getId() != null
                && this.user.getId().equals(userId);
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    public boolean isCanceled() {
        return this.status == ReservationStatus.CANCELED;
    }
}
