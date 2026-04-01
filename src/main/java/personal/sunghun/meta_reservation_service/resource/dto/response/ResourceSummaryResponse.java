package personal.sunghun.meta_reservation_service.resource.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceSummaryResponse {
    private final Long id;
    private final String name;
    private final String location;
    private final Integer capacity;
}
