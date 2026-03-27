package personal.sunghun.meta_reservation_service.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import personal.sunghun.meta_reservation_service.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
