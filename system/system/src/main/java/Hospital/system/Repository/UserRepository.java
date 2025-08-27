package Hospital.system.Repository;

import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndRole(Long id, Role role);
    List<User> findByRole(Role role);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
