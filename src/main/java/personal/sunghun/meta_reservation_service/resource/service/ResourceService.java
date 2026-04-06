package personal.sunghun.meta_reservation_service.resource.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.sunghun.meta_reservation_service.common.exception.ResourceNotFoundException;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;
import personal.sunghun.meta_reservation_service.resource.repository.ResourceRepository;

@Service
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<Resource> getResources() {
        return resourceRepository.findAll();
    }

    public Resource getResourceDetail(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
