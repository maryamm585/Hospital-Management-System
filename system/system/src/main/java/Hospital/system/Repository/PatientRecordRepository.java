package Hospital.system.Repository;

import Hospital.system.Entity.PatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {
    List<PatientRecord> findByPatient_Id(Long patientId);
    List<PatientRecord> findByDoctor_Id(Long doctorId);

    @Query("SELECT p FROM PatientRecord p WHERE p.notes LIKE %:keyword%")
    List<PatientRecord> searchByNotes(String keyword);
}
