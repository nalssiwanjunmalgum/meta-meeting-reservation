package personal.sunghun.meta_reservation_service.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.sunghun.meta_reservation_service.resource.domain.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    // 목록 조회
    // 상세 조회
    // 예약 관련 회의실 존재 확인
}
