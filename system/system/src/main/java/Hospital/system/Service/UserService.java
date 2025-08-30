package Hospital.system.Service;

import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Entity.User;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User createUser(UserRegistrationDto dto) {
        log.debug("Creating user with email={}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("Email already exists: {}", dto.getEmail());
            throw new BadRequestException("Email already exists!");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password("default123") // You may replace with real password handling
                .role(null) // role can be set later
                .build();

        User saved = userRepository.save(user);
        log.info("User created successfully: userId={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Fetched {} users", users.size());
        return users;
    }

    public User getUserById(Long id) {
        log.debug("Fetching user by id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }

    public void deleteUser(Long id) {
        log.debug("Deleting user by id={}", id);

        if (!userRepository.existsById(id)) {
            log.error("User not found with id={}", id);
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully: userId={}", id);
    }
}
