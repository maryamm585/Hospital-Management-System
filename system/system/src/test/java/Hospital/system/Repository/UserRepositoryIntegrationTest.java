package Hospital.system.Repository;

import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User patientUser;
    private User doctorUser;


    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create and persist Patient
        patientUser = new User();
        patientUser.setName("Patient One");
        patientUser.setEmail("patient@example.com");
        patientUser.setPassword("password123");
        patientUser.setRole(Role.PATIENT);
        entityManager.persist(patientUser);

        // Create and persist Doctor
        doctorUser = new User();
        doctorUser.setName("Doctor One");
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword("password123");
        doctorUser.setRole(Role.DOCTOR);
        entityManager.persist(doctorUser);

        entityManager.flush();
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("patient@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Patient One");
        assertThat(found.get().getRole()).isEqualTo(Role.PATIENT);
    }

    @Test
    void testExistsByEmail() {
        boolean exists = userRepository.existsByEmail("doctor@example.com");
        boolean notExists = userRepository.existsByEmail("notfound@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByIdAndRole() {
        Optional<User> foundDoctor = userRepository.findByIdAndRole(doctorUser.getId(), Role.DOCTOR);
        Optional<User> wrongRole = userRepository.findByIdAndRole(doctorUser.getId(), Role.PATIENT);

        assertThat(foundDoctor).isPresent();
        assertThat(foundDoctor.get().getEmail()).isEqualTo("doctor@example.com");
        assertThat(wrongRole).isEmpty();
    }

    @Test
    void testFindByRole() {
        List<User> patients = userRepository.findByRole(Role.PATIENT);
        List<User> doctors = userRepository.findByRole(Role.DOCTOR);

        assertThat(patients).hasSize(1);
        assertThat(patients.get(0).getEmail()).isEqualTo("patient@example.com");

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0).getEmail()).isEqualTo("doctor@example.com");
    }
}
