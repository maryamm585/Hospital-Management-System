package Hospital.system.Service;

import Hospital.system.DTO.AppointmentDto;
import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Mapper.AppointmentMapper;
import Hospital.system.Repository.AppointmentRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    private final UserRepository userRepository;

    private static final LocalTime START_WORK = LocalTime.of(9, 0);
    private static final LocalTime END_WORK = LocalTime.of(21, 0);

    @Transactional
    public AppointmentDto bookAppointment(AppointmentDto appointmentDto) {
        log.info("Booking appointment for patientId={} with doctorId={} at {}", appointmentDto.getPatientId(), appointmentDto.getDoctorId(), appointmentDto.getAppointmentTime());
        User patient = userRepository.findByIdAndRole(appointmentDto.getPatientId(), Role.PATIENT)
                .orElseThrow(() -> {
                    log.error("Patient not found with id {}", appointmentDto.getPatientId());
                    return new ResourceNotFoundException("No Patient Exist with Id " + appointmentDto.getPatientId());
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });

        //patientId in DTO matches logged-in user
        if (!loggedInUser.getId().equals(appointmentDto.getPatientId())) {
            log.warn("Patient {} tried to book an appointment for another patient {}", loggedInUser.getId(), appointmentDto.getPatientId());
            throw new AccessDeniedException("Patient can only book for himself");
        }
        User doctor = userRepository.findByIdAndRole(appointmentDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(() -> {
                    log.error("Doctor not found with id {}", appointmentDto.getDoctorId());
                    return new ResourceNotFoundException("No Doctor Exist with Id " + appointmentDto.getDoctorId());
                });
        validateAppointmentTime(appointmentDto.getAppointmentTime(), doctor.getId());

        //save appointment
        Appointment appointment = AppointmentMapper.toEntity(appointmentDto, doctor, patient);

        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment booked successfully with id={}", saved.getId());

        return AppointmentMapper.toDto(saved);
    }

//    @Transactional
//    public List<AppointmentDto> getAllAppointments(){
//        return appointmentRepository.findAll().stream()
//                .map(AppointmentMapper::toDto)
//                .toList();
//    }
//
//    @Transactional
//    public List<AppointmentDto> getAllAppointmentsByStatus(AppointmentStatus status){
//        return appointmentRepository.findByStatus(status).stream()
//                .map(AppointmentMapper::toDto)
//                .toList();
//    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByPatient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching appointments for patient: email={}", email);
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found by email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        List<AppointmentDto> appointments = appointmentRepository.findByPatient_Id(patient.getId()).stream()
                .map(AppointmentMapper::toDto)
                .toList();
        log.info("Found {} appointments for patient {}", appointments.size(), patient.getId());
        return appointments;
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByPatientAndStatus(AppointmentStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching {} appointments for patient: email={}", status, email);
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found by email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        List<AppointmentDto> appointments = appointmentRepository.findByPatient_IdAndStatus(patient.getId(), status).stream()
                .map(AppointmentMapper::toDto)
                .toList();
        log.info("Found {} {} appointments for patient {}", appointments.size(), status, patient.getId());
        return appointments;
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByDoctor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching appointments for doctor: email={}", email);

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor not found by email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        List<AppointmentDto> appointments = appointmentRepository.findByDoctor_Id(doctor.getId()).stream()
                .map(AppointmentMapper::toDto)
                .toList();
        log.info("Found {} appointments for doctor {}", appointments.size(), doctor.getId());
        return appointments;
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByDoctorAndStatus(AppointmentStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching {} appointments for doctor: email={}", status, email);

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor not found by email={}", email);
                    return new ResourceNotFoundException("User not found");
                });
        List<AppointmentDto> appointments = appointmentRepository.findByDoctor_IdAndStatus(doctor.getId(), status).stream()
                .map(AppointmentMapper::toDto)
                .toList();
        log.info("Found {} {} appointments for doctor {}", appointments.size(), status, doctor.getId());
        return appointments;
    }

    @Transactional
    public AppointmentDto updateAppointment(Long appointmentId, AppointmentDto updatedDto) {
        log.info("Updating appointment: id={}, requestedBy={}", appointmentId,
                SecurityContextHolder.getContext().getAuthentication().getName());

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found: id={}", appointmentId);
                    return new ResourceNotFoundException("Appointment not found with id " + appointmentId);
                });
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged-in user not found: email={}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        log.debug("Current appointment status={}, loggedInUserRole={}", appointment.getStatus(), loggedInUser.getRole());

        // Only allow update if status is PENDING or BOOKED
        if (appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.BOOKED) {
            log.warn("Invalid update attempt: status={}, appointmentId={}", appointment.getStatus(), appointmentId);
            throw new IllegalArgumentException("Only Pending or Booked appointments can be updated");
        }

        // Handle role-specific logic
        if (loggedInUser.getRole() == Role.PATIENT) {
            // Patient can only update their own appointment
            if (!appointment.getPatient().getId().equals(loggedInUser.getId())) {
                log.warn("Patient {} tried to update appointment of patient {}",
                        loggedInUser.getId(), appointment.getPatient().getId());
                throw new AccessDeniedException("You cannot update another patient's appointment");
            }

            // Ignore any patientId changes
            if (updatedDto.getPatientId() != null && !updatedDto.getPatientId().equals(loggedInUser.getId())) {
                log.warn("Patient {} tried to change id to that of patient {}",
                        loggedInUser.getId(), updatedDto.getPatientId());
                throw new BadRequestException("Patients cannot change the patient ID");
            }

            // Allow changing doctorId if valid
            if (updatedDto.getDoctorId() != null && !updatedDto.getDoctorId().equals(appointment.getDoctor().getId())) {
                User newDoctor = userRepository.findByIdAndRole(updatedDto.getDoctorId(), Role.DOCTOR)
                        .orElseThrow(() -> {
                            log.error("Doctor not found by id={}", updatedDto.getDoctorId());
                            return new ResourceNotFoundException("Doctor not found with id " + updatedDto.getDoctorId());
                        });
                appointment.setDoctor(newDoctor);
            }

            appointment.setPatient(loggedInUser); // enforce patient ID
        } else if (loggedInUser.getRole() == Role.DOCTOR) {
            // Doctors can only update their own appointment
            if (!appointment.getDoctor().getId().equals(loggedInUser.getId())) {
                log.warn("Doctor {} tried to update appointment of doctor {}",
                        loggedInUser.getId(), appointment.getDoctor().getId());
                throw new AccessDeniedException("You cannot update another doctor's appointment");
            }
            // Doctor cannot change doctorId
            if (updatedDto.getDoctorId() != null && !updatedDto.getDoctorId().equals(loggedInUser.getId())) {
                log.warn("Doctor {} tried to change id to that of docotor {}",
                        loggedInUser.getId(), updatedDto.getDoctorId());
                throw new BadRequestException("Doctors cannot change the doctor ID");
            }

            // Allow changing patientId if valid
            if (updatedDto.getPatientId() != null && !updatedDto.getPatientId().equals(appointment.getPatient().getId())) {
                User newPatient = userRepository.findByIdAndRole(updatedDto.getPatientId(), Role.PATIENT)
                        .orElseThrow(() -> {
                            log.error("Patient not found by id={}", updatedDto.getPatientId());
                            return new ResourceNotFoundException("Patient not found with id " + updatedDto.getPatientId());
                        });
                appointment.setPatient(newPatient);
            }
            appointment.setDoctor(loggedInUser); // enforce patient ID
        } else { //should never reach here cause of security jwt.
            throw new AccessDeniedException("Only patients or doctors can update appointments");
        }

        // Update appointment time if provided
        if (updatedDto.getAppointmentTime() != null) {
            validateAppointmentTime(updatedDto.getAppointmentTime(), appointment.getDoctor().getId());
            appointment.setAppointmentTime(updatedDto.getAppointmentTime());
        }

        // Revert status if it was BOOKED and patient is the one updating
        if (appointment.getStatus() == AppointmentStatus.BOOKED && loggedInUser.getRole() == Role.PATIENT) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully: id={}, byUser={}", saved.getId(), loggedInUser.getId());
        return AppointmentMapper.toDto(saved);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        //soft delete => change status to CANCELLED
        //cancel only if booked or pending
        log.info("Cancelling appointment: id={}, requestedBy={}", appointmentId,
                SecurityContextHolder.getContext().getAuthentication().getName());

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found: id={}", appointmentId);
                    return new ResourceNotFoundException("Appointment not found with id " + appointmentId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Current user not found: email={}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        if (!(appointment.getStatus() == AppointmentStatus.PENDING ||
                appointment.getStatus() == AppointmentStatus.BOOKED)) {
            log.warn("Cancel attempt failed: invalid status={}, appointmentId={}", appointment.getStatus(), appointmentId);
            throw new BadRequestException("Appointment cannot be cancelled (status = " + appointment.getStatus() + ")");
        }

        if (currentUser.getRole() == Role.PATIENT) {
            // Patient can only cancel their own appointment
            if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                log.warn("Patient {} tried to cancel another patient's appointment {}", currentUser.getId(), appointmentId);
                throw new AccessDeniedException("Patients can only cancel their own appointments");
            }
        } else if (currentUser.getRole() == Role.DOCTOR) {
            // Doctor can only cancel their own appointment
            if (!appointment.getDoctor().getId().equals(currentUser.getId())) {
                log.warn("Doctor {} tried to cancel another doctor's appointment {}", currentUser.getId(), appointmentId);
                throw new AccessDeniedException("Doctors can only cancel their own appointments");
            }
        } else { //shouldn't reach here
            throw new AccessDeniedException("Only doctors or patients can cancel appointments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully: id={}, byUser={}", appointmentId, currentUser.getId());
    }

    @Transactional
    public AppointmentDto approveAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found: id={}", appointmentId);
                    return new ResourceNotFoundException("Appointment not found with id " + appointmentId);
                });
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor with email={} not found", email);
                    return new ResourceNotFoundException("User not found");
                });
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            log.warn("Doctor {} attempted to approve appointment id={} that is not assigned to them", doctor.getId(), appointmentId);
            throw new AccessDeniedException("Doctors can only approve their own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            log.warn("Appointment id={} cannot be approved because current status={}", appointmentId, appointment.getStatus());
            throw new BadRequestException("Cannot approve appointment that is not PENDING");
        }

        //only future appointments can be approved
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            log.warn("Appointment in the past: start={}, doctorId={}", appointment.getAppointmentTime(), doctor.getId());
            throw new BadRequestException("Cannot approve an appointment in the past");
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment id={} successfully approved by doctor={}", appointmentId, doctor.getId());
        return AppointmentMapper.toDto(saved);
    }

    @Transactional
    public AppointmentDto completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found: id={}", appointmentId);
                    return new ResourceNotFoundException("Appointment not found with id " + appointmentId);
                });
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor with email={} not found", email);
                    return new ResourceNotFoundException("User not found");
                });
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            log.warn("Doctor {} attempted to approve appointment id={} that is not assigned to them", doctor.getId(), appointmentId);
            throw new AccessDeniedException("Doctors can only complete their own appointments");
        }

        if (!appointment.getStatus().equals(AppointmentStatus.BOOKED)) {
            log.warn("Appointment id={} cannot be completed because current status={}", appointmentId, appointment.getStatus());
            throw new BadRequestException("Can't complete a not BOOKED appointment");
        }

        //only past appointments can be marked as completed
        if (appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
            log.warn("Appointment still in the future: start={}, doctorId={}", appointment.getAppointmentTime(), doctor.getId());
            throw new BadRequestException("Cannot mark an appointment as completed before its time");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment id={} successfully completed by doctor={}", appointmentId, doctor.getId());
        return AppointmentMapper.toDto(saved);
    }


    private void validateAppointmentTime(LocalDateTime start, Long doctorId) {
        log.debug("Validating appointment time={} for doctorId={}", start, doctorId);
        LocalDateTime end = start.plusHours(1);

        // must be in the future
        if (start.isBefore(LocalDateTime.now())) {
            log.warn("Appointment in the past: start={}, doctorId={}", start, doctorId);
            throw new BadRequestException("Appointment must be in the future");
        }

        // working hours check (full LocalDateTime)
        LocalDateTime dayStart = start.toLocalDate().atTime(START_WORK);
        LocalDateTime dayEnd = start.toLocalDate().atTime(END_WORK);
        if (start.isBefore(dayStart) || end.isAfter(dayEnd)) {
            log.warn("Appointment outside working hours: start={}, doctorId={}", start, doctorId);
            throw new BadRequestException("Doctor works only between 09:00 and 21:00");
        }

        // enforce whole-hour slots
        if (start.getMinute() != 0 || start.getSecond() != 0) {
            log.warn("Appointment not aligned to full hour: start={}, doctorId={}", start, doctorId);
            throw new BadRequestException("Appointments must start at the top of the hour");
        }

        // conflict check
        boolean conflict = appointmentRepository.existsOverlappingAppointment(doctorId, start, end) != 0;
        if (conflict) {
            log.warn("Appointment conflict: start={}, doctorId={}", start, doctorId);
            throw new BadRequestException("Doctor already has an appointment at this time");
        }
        log.debug("Appointment time validated: start={}, doctorId={}", start, doctorId);

    }

}
