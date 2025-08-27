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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    private final UserRepository userRepository;

    private static final LocalTime START_WORK = LocalTime.of(9, 0);
    private static final LocalTime END_WORK = LocalTime.of(21, 0);

    @Transactional
    public AppointmentDto bookAppointment(AppointmentDto appointmentDto){
        User patient = userRepository.findByIdAndRole(appointmentDto.getPatientId(), Role.PATIENT)
                .orElseThrow(()-> new ResourceNotFoundException("No Patient Exist with Id "+ appointmentDto.getPatientId()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        //patientId in DTO matches logged-in user
        if (!loggedInUser.getId().equals(appointmentDto.getPatientId())) {
            throw new AccessDeniedException("Patient can only book for himself");
        }
        User doctor = userRepository.findByIdAndRole(appointmentDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(()-> new ResourceNotFoundException("No Doctor Exist with Id "+ appointmentDto.getDoctorId()));

        validateAppointmentTime(appointmentDto.getAppointmentTime(), doctor.getId());

        //save appointment
        Appointment appointment = AppointmentMapper.toEntity(appointmentDto,doctor,patient);

        Appointment saved = appointmentRepository.save(appointment);

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
    public List<AppointmentDto> getAllAppointmentsByPatient(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return appointmentRepository.findByPatient_Id(patient.getId()).stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByPatientAndStatus(AppointmentStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return appointmentRepository.findByPatient_IdAndStatus(patient.getId(),status).stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByDoctor(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return appointmentRepository.findByDoctor_Id(doctor.getId()).stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    @Transactional
    public List<AppointmentDto> getAllAppointmentsByDoctorAndStatus(AppointmentStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return appointmentRepository.findByDoctor_IdAndStatus(doctor.getId(),status).stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    @Transactional
    public AppointmentDto updateAppointment(Long appointmentId, AppointmentDto updatedDto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        // Only allow update if status is PENDING or BOOKED
        if (appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException("Only Pending or Booked appointments can be updated");
        }

        // Handle role-specific logic
        if (loggedInUser.getRole() == Role.PATIENT) {
            // Patient can only update their own appointment
            if (!appointment.getPatient().getId().equals(loggedInUser.getId())) {
                throw new AccessDeniedException("You cannot update another patient's appointment");
            }

            // Ignore any patientId changes
            if (updatedDto.getPatientId() != null && !updatedDto.getPatientId().equals(loggedInUser.getId())) {
                throw new BadRequestException("Patients cannot change the patient ID");
            }

            // Allow changing doctorId if valid
            if (updatedDto.getDoctorId() != null && !updatedDto.getDoctorId().equals(appointment.getDoctor().getId())) {
                User newDoctor = userRepository.findByIdAndRole(updatedDto.getDoctorId(), Role.DOCTOR)
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + updatedDto.getDoctorId()));
                appointment.setDoctor(newDoctor);
            }

            appointment.setPatient(loggedInUser); // enforce patient ID
        }
        else if (loggedInUser.getRole() == Role.DOCTOR) {
            // Doctors can only update their own appointment
            if (!appointment.getDoctor().getId().equals(loggedInUser.getId())) {
                throw new AccessDeniedException("You cannot update another doctor's appointment");
            }
            // Doctor cannot change doctorId
            if (updatedDto.getDoctorId() != null && !updatedDto.getDoctorId().equals(loggedInUser.getId())) {
                throw new BadRequestException("Doctors cannot change the doctor ID");
            }

            // Allow changing patientId if valid
            if (updatedDto.getPatientId() != null && !updatedDto.getPatientId().equals(appointment.getPatient().getId())) {
                User newPatient = userRepository.findByIdAndRole(updatedDto.getPatientId(), Role.PATIENT)
                        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + updatedDto.getPatientId()));
                appointment.setPatient(newPatient);
            }
            appointment.setDoctor(loggedInUser); // enforce patient ID
        }
        else {
            throw new AccessDeniedException("Only patients or doctors can update appointments");
        }

        // Update appointment time if provided
        if (updatedDto.getAppointmentTime() != null) {
            validateAppointmentTime(updatedDto.getAppointmentTime(), appointment.getDoctor().getId());
            appointment.setAppointmentTime(updatedDto.getAppointmentTime());
        }

        // Revert status if it was BOOKED and patient is the one updating
        if (appointment.getStatus() == AppointmentStatus.BOOKED && loggedInUser.getRole()==Role.PATIENT) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDto(saved);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId){
        //soft delete => change status to cancelled
        //cancel only if booked or pending
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        if (!(appointment.getStatus() == AppointmentStatus.PENDING ||
                appointment.getStatus() == AppointmentStatus.BOOKED)) {
            throw new BadRequestException("Appointment cannot be cancelled (status = " + appointment.getStatus() + ")");
        }

        if (currentUser.getRole() == Role.PATIENT) {
            // Patient can only cancel their own appointment
            if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Patients can only cancel their own appointments");
            }
        } else if (currentUser.getRole() == Role.DOCTOR) {
            // Doctor can only cancel their own appointment
            if (!appointment.getDoctor().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Doctors can only cancel their own appointments");
            }
        } else {
            throw new AccessDeniedException("Only doctors or patients can cancel appointments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentDto approveAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Doctors can only approve their own appointments");
        }

        if(appointment.getStatus()!= AppointmentStatus.PENDING){
            throw new BadRequestException("Cannot approve appointment that is not PENDING");
        }

        //only future appointments can be approved
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot approve an appointment in the past");
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDto(saved);
    }

    @Transactional
    public AppointmentDto completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Doctors can only complete their own appointments");
        }

        if(!appointment.getStatus().equals(AppointmentStatus.BOOKED)){
            throw new BadRequestException("Can't complete a not BOOKED appointment");
        }

        //only past appointments can be marked as completed
        if (appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot mark an appointment as completed before its time");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDto(saved);
    }



    private void validateAppointmentTime(LocalDateTime start, Long doctorId) {
        LocalDateTime end = start.plusHours(1);

        // must be in the future
        if (start.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment must be in the future");
        }

        // working hours check (full LocalDateTime)
        LocalDateTime dayStart = start.toLocalDate().atTime(START_WORK);
        LocalDateTime dayEnd = start.toLocalDate().atTime(END_WORK);
        if (start.isBefore(dayStart) || end.isAfter(dayEnd)) {
            throw new BadRequestException("Doctor works only between 09:00 and 21:00");
        }

        // enforce whole-hour slots
        if (start.getMinute() != 0 || start.getSecond() != 0) {
            throw new BadRequestException("Appointments must start at the top of the hour");
        }

        // conflict check
        boolean conflict = appointmentRepository.existsOverlappingAppointment(doctorId, start, end) != 0;
        if (conflict) {
            throw new BadRequestException("Doctor already has an appointment at this time");
        }
    }

}
