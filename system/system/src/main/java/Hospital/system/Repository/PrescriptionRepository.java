package Hospital.system.Repository;

import Hospital.system.Entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient_Id(Long patientId);
    List<Prescription> findByDoctor_Id(Long doctorId);
    List<Prescription> findByMedicine_Id(Long medicineId);
}
