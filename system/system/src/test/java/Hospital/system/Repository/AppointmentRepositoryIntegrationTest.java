package Hospital.system.Repository;

import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AppointmentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private User doctor;
    private User patient;
    private Appointment appointment1;
    private Appointment appointment2;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create doctor
        doctor = new User();
        doctor.setName("Doctor One");
        doctor.setEmail("doctor@example.com");
        doctor.setPassword("password123");
        doctor.setRole(Role.DOCTOR);
        entityManager.persist(doctor);

        // Create patient
        patient = new User();
        patient.setName("Patient One");
        patient.setEmail("patient@example.com");
        patient.setPassword("password123");
        patient.setRole(Role.PATIENT);
        entityManager.persist(patient);

        // Create Appointment 1 (PENDING)
        appointment1 = new Appointment();
        appointment1.setDoctor(doctor);
        appointment1.setPatient(patient);
        appointment1.setAppointmentTime(LocalDateTime.now().plusDays(1));
        appointment1.setStatus(AppointmentStatus.PENDING);
        entityManager.persist(appointment1);

        // Create Appointment 2 (APPROVED)
        appointment2 = new Appointment();
        appointment2.setDoctor(doctor);
        appointment2.setPatient(patient);
        appointment2.setAppointmentTime(LocalDateTime.now().plusDays(2));
        appointment2.setStatus(AppointmentStatus.COMPLETED);
        entityManager.persist(appointment2);

        entityManager.flush();
    }

    @Test
    void testFindByDoctorId() {
        List<Appointment> results = appointmentRepository.findByDoctor_Id(doctor.getId());
        assertThat(results).hasSize(2);
    }

    @Test
    void testFindByPatientId() {
        List<Appointment> results = appointmentRepository.findByPatient_Id(patient.getId());
        assertThat(results).hasSize(2);
    }

    @Test
    void testFindByStatus() {
        List<Appointment> pendingAppointments = appointmentRepository.findByStatus(AppointmentStatus.PENDING);
        assertThat(pendingAppointments).extracting("status").contains(AppointmentStatus.PENDING);
    }

    @Test
    void testFindByPatientIdAndStatus() {
        List<Appointment> results = appointmentRepository.findByPatient_IdAndStatus(patient.getId(), AppointmentStatus.COMPLETED);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void testFindByDoctorIdAndStatus() {
        List<Appointment> results = appointmentRepository.findByDoctor_IdAndStatus(doctor.getId(), AppointmentStatus.PENDING);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void testFindByDoctorIdAndAppointmentTimeBetween() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        List<Appointment> results = appointmentRepository.findByDoctor_IdAndAppointmentTimeBetween(
                doctor.getId(), start, end);

        assertThat(results).hasSize(2);
    }

    @Test
    void testFindAppointmentsForDoctorExcludingStatus() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        List<Appointment> results = appointmentRepository.findAppointmentsForDoctorExcludingStatus(
                doctor.getId(), AppointmentStatus.PENDING, start, end);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }
}
