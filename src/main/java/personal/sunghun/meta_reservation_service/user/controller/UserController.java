package personal.sunghun.meta_reservation_service.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.sunghun.meta_reservation_service.user.domain.User;
import personal.sunghun.meta_reservation_service.user.dto.request.UserLoginRequest;
import personal.sunghun.meta_reservation_service.user.dto.request.UserSignUpRequest;
import personal.sunghun.meta_reservation_service.user.dto.response.UserLoginResponse;
import personal.sunghun.meta_reservation_service.user.dto.response.UserSignUpResponse;
import personal.sunghun.meta_reservation_service.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponse> signUp(
            @Valid @RequestBody UserSignUpRequest request
    ) {
        User user = userService.signUp(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        UserSignUpResponse response = new UserSignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        userService.login(
                request.getEmail(),
                request.getPassword()
        );

        UserLoginResponse response = new UserLoginResponse("로그인에 성공했습니다.");
        return ResponseEntity.ok(response);
    }
}
