package Hospital.system.Repository;

import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor_Id(Long doctorId);
    List<Appointment> findByPatient_Id(Long patientId);
    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatient_IdAndStatus(Long patientId, AppointmentStatus status);
    List<Appointment> findByDoctor_IdAndStatus(Long doctorId, AppointmentStatus status);
    List<Appointment> findByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    @Query(value = """
    SELECT EXISTS(
        SELECT 1
        FROM appointments a
        WHERE a.doctor_id = :doctorId
          AND a.status IN ('PENDING','BOOKED')
          AND a.appointment_time < :newAppointmentEnd
          AND DATE_ADD(a.appointment_time, INTERVAL 1 HOUR) > :newAppointmentStart
    )
""", nativeQuery = true)
    Long existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("newAppointmentStart") LocalDateTime newAppointmentStart,
            @Param("newAppointmentEnd") LocalDateTime newAppointmentEnd
    );

    // fetch appointments for a doctor excluding a specific status within a time range
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.status <> :excludedStatus " +
            "AND a.appointmentTime >= :startTime " +
            "AND a.appointmentTime < :endTime")
    List<Appointment> findAppointmentsForDoctorExcludingStatus(
            @Param("doctorId") Long doctorId,
            @Param("excludedStatus") AppointmentStatus excludedStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


}
