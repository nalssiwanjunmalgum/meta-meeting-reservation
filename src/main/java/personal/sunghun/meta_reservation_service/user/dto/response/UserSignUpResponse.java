package personal.sunghun.meta_reservation_service.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserSignUpResponse {
    private final Long id;
    private final String email;
    private final String name;
}
