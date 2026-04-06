package personal.sunghun.meta_reservation_service.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.sunghun.meta_reservation_service.common.exception.DuplicateUserEmailException;
import personal.sunghun.meta_reservation_service.common.exception.InvalidLoginException;
import personal.sunghun.meta_reservation_service.user.domain.User;
import personal.sunghun.meta_reservation_service.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User signUp(String name, String email, String password) {
        validateDuplicateEmail(email);

        User user = new User(name, email, password);
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidLoginException::new);

        validatePassword(user, password);
        return user;
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserEmailException();
        }
    }

    private void validatePassword(User user, String password) {
        if (!user.isPasswordMatch(password)) {
            throw new InvalidLoginException();
        }
    }
}
