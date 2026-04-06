package personal.sunghun.meta_reservation_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import personal.sunghun.meta_reservation_service.common.config.JpaAuditingConfig;
import personal.sunghun.meta_reservation_service.reservation.domain.Reservation;
import personal.sunghun.meta_reservation_service.reservation.domain.ReservationStatus;
import personal.sunghun.meta_reservation_service.reservation.repository.ReservationRepository;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.resource.repository.ResourceRepository;
import personal.sunghun.meta_reservation_service.user.domain.User;
import personal.sunghun.meta_reservation_service.user.repository.UserRepository;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    @DisplayName("같은 리소스에 겹치는 예약이 있으면 true를 반환한다")
    void existsOverlappingReservation_returnsTrue_whenOverlappingExists() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resource = saveResource("회의실A", "3층", 8, "대회의실");

        saveReservation(
                user,
                resource,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.RESERVED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                LocalDateTime.of(2026, 4, 10, 10, 30),
                LocalDateTime.of(2026, 4, 10, 11, 30),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("같은 리소스라도 시간이 겹치지 않으면 false를 반환한다")
    void existsOverlappingReservation_returnsFalse_whenNotOverlapping() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resource = saveResource("회의실A", "3층", 8, "대회의실");

        saveReservation(
                user,
                resource,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.RESERVED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 리소스의 예약이면 같은 시간이라도 false를 반환한다")
    void existsOverlappingReservation_returnsFalse_whenResourceIsDifferent() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resourceA = saveResource("회의실A", "3층", 8, "대회의실");
        Resource resourceB = saveResource("회의실B", "4층", 6, "중회의실");

        saveReservation(
                user,
                resourceA,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.RESERVED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resourceB.getId(),
                LocalDateTime.of(2026, 4, 10, 10, 30),
                LocalDateTime.of(2026, 4, 10, 11, 30),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("취소된 예약은 충돌 검사에서 제외하므로 false를 반환한다")
    void existsOverlappingReservation_returnsFalse_whenOnlyCanceledReservationExists() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resource = saveResource("회의실A", "3층", 8, "대회의실");

        saveReservation(
                user,
                resource,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.CANCELED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                LocalDateTime.of(2026, 4, 10, 10, 30),
                LocalDateTime.of(2026, 4, 10, 11, 30),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기존 예약 종료 시간과 요청 시작 시간이 같으면 겹치지 않으므로 false를 반환한다")
    void existsOverlappingReservation_returnsFalse_whenExistingEndEqualsRequestStart() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resource = saveResource("회의실A", "3층", 8, "대회의실");

        saveReservation(
                user,
                resource,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.RESERVED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기존 예약 시작 시간과 요청 종료 시간이 같으면 겹치지 않으므로 false를 반환한다")
    void existsOverlappingReservation_returnsFalse_whenExistingStartEqualsRequestEnd() {
        // given
        User user = saveUser("tester@test.com", "1234", "tester");
        Resource resource = saveResource("회의실A", "3층", 8, "대회의실");

        saveReservation(
                user,
                resource,
                LocalDateTime.of(2026, 4, 10, 10, 0),
                LocalDateTime.of(2026, 4, 10, 11, 0),
                ReservationStatus.RESERVED
        );

        // when
        boolean result = reservationRepository.existsOverlappingReservation(
                resource.getId(),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0),
                ReservationStatus.CANCELED
        );

        // then
        assertThat(result).isFalse();
    }

    private User saveUser(String email, String password, String name) {
        User user = new User(name, email, password);
        return userRepository.save(user);
    }

    private Resource saveResource(String name, String location, Integer capacity, String description) {
        Resource resource = new Resource(name, location, capacity, description);
        return resourceRepository.save(resource);
    }

    private Reservation saveReservation(
            User user,
            Resource resource,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    ) {
        Reservation reservation = new Reservation(user, resource, startTime, endTime, status);
        return reservationRepository.save(reservation);
    }
}
