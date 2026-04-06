package personal.sunghun.meta_reservation_service.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import personal.sunghun.meta_reservation_service.common.exception.GlobalExceptionHandler;
import personal.sunghun.meta_reservation_service.reservation.controller.ReservationController;
import personal.sunghun.meta_reservation_service.reservation.domain.Reservation;
import personal.sunghun.meta_reservation_service.reservation.domain.ReservationStatus;
import personal.sunghun.meta_reservation_service.reservation.service.ReservationService;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.user.domain.User;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("예약 생성")
    class CreateReservationTest {

        @Test
        @DisplayName("정상 요청이면 201 Created와 예약 생성 응답을 반환한다")
        void createReservation_success() throws Exception {
            // given
            Long userId = 1L;
            Long resourceId = 10L;
            LocalDateTime startAt = LocalDateTime.of(2026, 4, 10, 10, 0);
            LocalDateTime endAt = LocalDateTime.of(2026, 4, 10, 11, 0);

            ReservationCreateRequestFixture request = new ReservationCreateRequestFixture(resourceId, startAt, endAt);

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(resourceId, "회의실A", "3층", 8, "대회의실");
            Reservation reservation = createReservation(100L, user, resource, startAt, endAt, ReservationStatus.RESERVED);

            given(reservationService.createReservation(
                    eq(userId), eq(resourceId), eq(startAt), eq(endAt)
            )).willReturn(reservation);

            // when & then
            mockMvc.perform(post("/api/reservations")
                            .param("userId", String.valueOf(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reservationId").value(100L))
                    .andExpect(jsonPath("$.resourceId").value(resourceId))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.startAt").value("2026-04-10T10:00:00"))
                    .andExpect(jsonPath("$.endAt").value("2026-04-10T11:00:00"))
                    .andExpect(jsonPath("$.status").value("RESERVED"));
        }

        @Test
        @DisplayName("resourceId가 없으면 400 Bad Request를 반환한다")
        void createReservation_returnsBadRequest_whenResourceIdIsNull() throws Exception {
            // given
            String requestBody = """
                    {
                      "startAt": "2026-04-10T10:00:00",
                      "endAt": "2026-04-10T11:00:00"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/reservations")
                            .param("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("startAt이 없으면 400 Bad Request를 반환한다")
        void createReservation_returnsBadRequest_whenStartAtIsNull() throws Exception {
            // given
            String requestBody = """
                    {
                      "resourceId": 10,
                      "endAt": "2026-04-10T11:00:00"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/reservations")
                            .param("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("endAt이 없으면 400 Bad Request를 반환한다")
        void createReservation_returnsBadRequest_whenEndAtIsNull() throws Exception {
            // given
            String requestBody = """
                    {
                      "resourceId": 10,
                      "startAt": "2026-04-10T10:00:00"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/reservations")
                            .param("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("startAt이 현재보다 과거면 400 Bad Request를 반환한다")
        void createReservation_returnsBadRequest_whenStartAtIsPast() throws Exception {
            // given
            LocalDateTime past = LocalDateTime.now().minusDays(1);
            LocalDateTime future = LocalDateTime.now().plusDays(1);

            ReservationCreateRequestFixture request = new ReservationCreateRequestFixture(10L, past, future);

            // when & then
            mockMvc.perform(post("/api/reservations")
                            .param("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("내 예약 조회")
    class GetMyReservationsTest {

        @Test
        @DisplayName("정상 요청이면 200 OK와 예약 목록을 반환한다")
        void getMyReservations_success() throws Exception {
            // given
            Long userId = 1L;
            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation reservation1 = createReservation(
                    101L,
                    user,
                    resource,
                    LocalDateTime.of(2026, 4, 10, 10, 0),
                    LocalDateTime.of(2026, 4, 10, 11, 0),
                    ReservationStatus.RESERVED
            );

            Reservation reservation2 = createReservation(
                    102L,
                    user,
                    resource,
                    LocalDateTime.of(2026, 4, 11, 14, 0),
                    LocalDateTime.of(2026, 4, 11, 15, 0),
                    ReservationStatus.CANCELED
            );

            given(reservationService.getMyReservations(userId))
                    .willReturn(List.of(reservation1, reservation2));

            // when & then
            mockMvc.perform(get("/api/reservations/my")
                            .param("userId", String.valueOf(userId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].reservationId").value(101L))
                    .andExpect(jsonPath("$[0].resourceId").value(10L))
                    .andExpect(jsonPath("$[0].resourceName").value("회의실A"))
                    .andExpect(jsonPath("$[0].status").value("RESERVED"))
                    .andExpect(jsonPath("$[1].reservationId").value(102L))
                    .andExpect(jsonPath("$[1].status").value("CANCELED"));
        }
    }

    @Nested
    @DisplayName("예약 취소")
    class CancelReservationTest {

        @Test
        @DisplayName("정상 요청이면 200 OK와 취소 응답을 반환한다")
        void cancelReservation_success() throws Exception {
            // given
            Long userId = 1L;
            Long reservationId = 100L;

            User user = createUser(userId, "tester", "tester@test.com", "1234");
            Resource resource = createResource(10L, "회의실A", "3층", 8, "대회의실");

            Reservation canceledReservation = createReservation(
                    reservationId,
                    user,
                    resource,
                    LocalDateTime.of(2026, 4, 10, 10, 0),
                    LocalDateTime.of(2026, 4, 10, 11, 0),
                    ReservationStatus.CANCELED
            );

            given(reservationService.cancelReservation(userId, reservationId))
                    .willReturn(canceledReservation);

            // when & then
            mockMvc.perform(post("/api/reservations/{reservationId}/cancel", reservationId)
                            .param("userId", String.valueOf(userId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reservationId").value(reservationId))
                    .andExpect(jsonPath("$.status").value("CANCELED"));
        }
    }

    /**
     * ReservationCreateRequest는 protected no-args 생성자 + private 필드 구조라
     * 테스트에서 request body 직렬화를 쉽게 하기 위한 fixture 클래스
     */
    private record ReservationCreateRequestFixture(
            Long resourceId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
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

    private Reservation createReservation(
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
