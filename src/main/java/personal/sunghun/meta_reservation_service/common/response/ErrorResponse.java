package personal.sunghun.meta_reservation_service.common.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp; // 에러 발생한 시각
    private final int status; // HTTP status code
    private final String code; // 비즈니스 에러
    private final String message; // description (멘트로 적용되는 메세지)
    private final String path; // 에러가 발생한 경로 포함

    private ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String code,
            String message,
            String path
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public static ErrorResponse of(
            int status,
            String code,
            String message,
            String path
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                code,
                message,
                path
        );
    }
}
