package Hospital.system.Repository;

import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor_Id(Long doctorId);
    List<Appointment> findByPatient_Id(Long patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
