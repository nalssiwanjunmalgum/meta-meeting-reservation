package personal.sunghun.meta_reservation_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import personal.sunghun.meta_reservation_service.reservation.service.ReservationService;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.resource.repository.ResourceRepository;
import personal.sunghun.meta_reservation_service.user.domain.User;
import personal.sunghun.meta_reservation_service.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Nested
    @DisplayName("예약 생성")
    class CreateReservationTest {

        @Test
        @DisplayName("정상적인 요청이면 예약을 생성한다")
        void createReservation_success() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1);
            LocalDateTime endAt = startAt.plusHours(2);

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(resourceId, "회의실A", "3층", 8, "대회의실");

            Reservation savedReservation = createReservationEntity(
                    100L, user, resource, startAt, endAt, ReservationStatus.RESERVED
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
            given(reservationRepository.existsOverlappingReservation(
                    eq(resourceId), eq(startAt), eq(endAt), eq(ReservationStatus.CANCELED)
            )).willReturn(false);

            given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);

            // when
            Reservation result = reservationService.createReservation(userId, resourceId, startAt, endAt);

            // then
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getUser().getId()).isEqualTo(userId);
            assertThat(result.getResource().getId()).isEqualTo(resourceId);
            assertThat(result.getStartTime()).isEqualTo(startAt);
            assertThat(result.getEndTime()).isEqualTo(endAt);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);

            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외가 발생한다")
        void createReservation_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1);
            LocalDateTime endAt = startAt.plusHours(1);

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation(userId, resourceId, startAt, endAt)
            ).isInstanceOf(UserNotFoundException.class);

            verify(resourceRepository, never()).findById(any());
            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 리소스면 예외가 발생한다")
        void createReservation_throwsResourceNotFoundException_whenResourceDoesNotExist() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1);
            LocalDateTime endAt = startAt.plusHours(1);

            User user = createUser(userId, "tester", "tester@test.com", "1234");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(resourceRepository.findById(resourceId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation(userId, resourceId, startAt, endAt)
            ).isInstanceOf(ResourceNotFoundException.class);

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("시작 시간이 종료 시간보다 늦으면 예외가 발생한다")
        void createReservation_throwsInvalidReservationTimeException_whenStartAtIsAfterEndAt() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1).plusHours(2);
            LocalDateTime endAt = LocalDateTime.now().plusDays(1);

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(resourceId, "회의실A", "3층", 8, "대회의실");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation(userId, resourceId, startAt, endAt)
            ).isInstanceOf(InvalidReservationTimeException.class);

            verify(reservationRepository, never()).existsOverlappingReservation(any(), any(), any(), any());
            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("시작 시간과 종료 시간이 같으면 예외가 발생한다")
        void createReservation_throwsInvalidReservationTimeException_whenStartAtEqualsEndAt() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1);

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(resourceId, "회의실A", "3층", 8, "대회의실");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation(userId, resourceId, startAt, startAt)
            ).isInstanceOf(InvalidReservationTimeException.class);

            verify(reservationRepository, never()).existsOverlappingReservation(any(), any(), any(), any());
            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("겹치는 예약이 존재하면 예외가 발생한다")
        void createReservation_throwsReservationConflictException_whenOverlappingReservationExists() {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.now().plusDays(1);
            LocalDateTime endAt = startAt.plusHours(2);

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(resourceId, "회의실A", "3층", 8, "대회의실");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(resourceRepository.findById(resourceId)).willReturn(Optional.of(resource));
            given(reservationRepository.existsOverlappingReservation(
                    eq(resourceId), eq(startAt), eq(endAt), eq(ReservationStatus.CANCELED)
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation(userId, resourceId, startAt, endAt)
            ).isInstanceOf(ReservationConflictException.class);

            verify(reservationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("내 예약 조회")
    class GetMyReservationsTest {

        @Test
        @DisplayName("사용자의 예약 목록을 반환한다")
        void getMyReservations_success() {
            // given
            Long userId = 1L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation1 = createReservationEntity(
                    101L, user, resource,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(1),
                    ReservationStatus.RESERVED
            );

            Reservation reservation2 = createReservationEntity(
                    102L, user, resource,
                    LocalDateTime.now().plusDays(2),
                    LocalDateTime.now().plusDays(2).plusHours(1),
                    ReservationStatus.RESERVED
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(reservationRepository.findByUser_IdOrderByStartTimeAsc(userId))
                    .willReturn(List.of(reservation1, reservation2));

            // when
            List<Reservation> result = reservationService.getMyReservations(userId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Reservation::getId)
                    .containsExactly(101L, 102L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외가 발생한다")
        void getMyReservations_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.getMyReservations(userId)
            ).isInstanceOf(UserNotFoundException.class);

            verify(reservationRepository, never()).findByUser_IdOrderByStartTimeAsc(any());
        }

        @Test
        @DisplayName("예약이 없으면 빈 리스트를 반환한다")
        void getMyReservations_returnsEmptyList_whenUserHasNoReservations() {
            // given
            Long userId = 1L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(reservationRepository.findByUser_IdOrderByStartTimeAsc(userId)).willReturn(List.of());

            // when
            List<Reservation> result = reservationService.getMyReservations(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("예약 취소")
    class CancelReservationTest {

        @Test
        @DisplayName("정상적인 요청이면 예약을 취소한다")
        void cancelReservation_success() {
            // given
            Long userId = 1L;
            Long reservationId = 100L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation = createReservationEntity(
                    reservationId,
                    user,
                    resource,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(1),
                    ReservationStatus.RESERVED
            );

            given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

            // when
            Reservation result = reservationService.cancelReservation(userId, reservationId);

            // then
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("존재하지 않는 예약이면 예외가 발생한다")
        void cancelReservation_throwsReservationNotFoundException_whenReservationDoesNotExist() {
            // given
            Long userId = 1L;
            Long reservationId = 100L;

            given(reservationRepository.findById(reservationId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancelReservation(userId, reservationId)
            ).isInstanceOf(ReservationNotFoundException.class);
        }

        @Test
        @DisplayName("본인 예약이 아니면 예외가 발생한다")
        void cancelReservation_throwsUnauthorizedReservationAccessException_whenReservationOwnerDiffers() {
            // given
            Long userId = 1L;
            Long reservationId = 100L;

            User owner = createUser(2L, "owner", "owner@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation = createReservationEntity(
                    reservationId,
                    owner,
                    resource,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(1),
                    ReservationStatus.RESERVED
            );

            given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancelReservation(userId, reservationId)
            ).isInstanceOf(UnauthorizedReservationAccessException.class);
        }

        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void cancelReservation_throwsAlreadyCanceledReservationException_whenReservationAlreadyCanceled() {
            // given
            Long userId = 1L;
            Long reservationId = 100L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation = createReservationEntity(
                    reservationId,
                    user,
                    resource,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(1),
                    ReservationStatus.CANCELED
            );

            given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancelReservation(userId, reservationId)
            ).isInstanceOf(AlreadyCanceledReservationException.class);
        }

        @Test
        @DisplayName("예약 시작 시간이 지나면 취소할 수 없다")
        void cancelReservation_throwsReservationCannotBeCanceledException_whenReservationAlreadyStarted() {
            // given
            Long userId = 1L;
            Long reservationId = 100L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation = createReservationEntity(
                    reservationId,
                    user,
                    resource,
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    ReservationStatus.RESERVED
            );

            given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancelReservation(userId, reservationId)
            ).isInstanceOf(ReservationCannotBeCanceledException.class);
        }
    }

    private User createUser(Long id, String name, String email, String password) {
        User user = new User(name, email, password);
        setField(user, "id", id);
        return user;
    }

    private Resource createResource(Long id, String name, String location, Integer capacity, String description) {
        Resource resource = new Resource(name, location, capacity, description);
        setField(resource, "id", id);
        return resource;
    }

    private Reservation createReservationEntity(
            Long id,
            User user,
            Resource resource,
            LocalDateTime startAt,
            LocalDateTime endAt,
            ReservationStatus status
    ) {
        Reservation reservation = new Reservation(user, resource, startAt, endAt, status);
        setField(reservation, "id", id);
        return reservation;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
