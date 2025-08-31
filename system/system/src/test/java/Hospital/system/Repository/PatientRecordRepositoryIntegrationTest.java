package Hospital.system.Repository;

import Hospital.system.Entity.PatientRecord;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PatientRecordRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRecordRepository patientRecordRepository;

    private User doctor;
    private User patient;
    private PatientRecord record1;
    private PatientRecord record2;

    @BeforeEach
    void setup() {
        patientRecordRepository.deleteAll();
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

        // Create PatientRecord 1
        record1 = new PatientRecord();
        record1.setDoctor(doctor);
        record1.setPatient(patient);
        record1.setNotes("Patient has flu symptoms");
        entityManager.persist(record1);

        // Create PatientRecord 2
        record2 = new PatientRecord();
        record2.setDoctor(doctor);
        record2.setPatient(patient);
        record2.setNotes("Routine check-up for blood pressure");
        entityManager.persist(record2);

        entityManager.flush();
    }


    @Test
    void testFindByPatientId() {
        List<PatientRecord> records = patientRecordRepository.findByPatient_Id(patient.getId());

        assertThat(records).hasSize(2);
        assertThat(records).extracting("patient.email").contains("patient@example.com");
    }

    @Test
    void testFindByDoctorId() {
        List<PatientRecord> records = patientRecordRepository.findByDoctor_Id(doctor.getId());

        assertThat(records).hasSize(2);
        assertThat(records).extracting("doctor.email").contains("doctor@example.com");
    }

    @Test
    void testSearchByNotes() {
        List<PatientRecord> fluRecords = patientRecordRepository.searchByNotes("flu");
        List<PatientRecord> pressureRecords = patientRecordRepository.searchByNotes("blood pressure");
        List<PatientRecord> notFoundRecords = patientRecordRepository.searchByNotes("diabetes");

        assertThat(fluRecords).hasSize(1);
        assertThat(fluRecords.get(0).getNotes()).contains("flu");

        assertThat(pressureRecords).hasSize(1);
        assertThat(pressureRecords.get(0).getNotes()).contains("blood pressure");

        assertThat(notFoundRecords).isEmpty();
    }
}
