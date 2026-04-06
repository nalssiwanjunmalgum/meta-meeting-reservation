package personal.sunghun.meta_reservation_service.resource.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.resource.dto.response.ResourceDetailResponse;
import personal.sunghun.meta_reservation_service.resource.dto.response.ResourceSummaryResponse;
import personal.sunghun.meta_reservation_service.resource.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<ResourceSummaryResponse>> getResources() {
        List<Resource> resources = resourceService.getResources();

        List<ResourceSummaryResponse> response = resources.stream()
                .map(this::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ResourceDetailResponse> getResourceDetail(
            @PathVariable Long resourceId
    ) {
        Resource resource = resourceService.getResourceDetail(resourceId);
        ResourceDetailResponse response = toDetailResponse(resource);

        return ResponseEntity.ok(response);
    }

    private ResourceSummaryResponse toSummaryResponse(Resource resource) {
        return new ResourceSummaryResponse(
                resource.getId(),
                resource.getName(),
                resource.getLocation(),
                resource.getCapacity()
        );
    }

    private ResourceDetailResponse toDetailResponse(Resource resource) {
        return new ResourceDetailResponse(
                resource.getId(),
                resource.getName(),
                resource.getLocation(),
                resource.getCapacity(),
                resource.getDescription()
        );
    }
}
