package Hospital.system.unit.Service;

import Hospital.system.DTO.AppointmentDto;
import Hospital.system.Entity.*;
import Hospital.system.Repository.AppointmentRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.AppointmentService;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patient;
    private User doctor;
    private AppointmentDto appointmentDto;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(1L)
                .email("patient@test.com")
                .role(Role.PATIENT)
                .name("Patient")
                .build();

        doctor = User.builder()
                .id(2L)
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .name("Doctor")
                .build();

        appointmentDto = new AppointmentDto();
        appointmentDto.setPatientId(1L);
        appointmentDto.setDoctorId(2L);
        appointmentDto.setAppointmentTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));

        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(appointmentDto.getAppointmentTime())
                .status(AppointmentStatus.PENDING)
                .build();
    }

    @Test
    void bookAppointment_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByIdAndRole(1L, Role.PATIENT)).thenReturn(Optional.of(patient));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.DOCTOR)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDto result = appointmentService.bookAppointment(appointmentDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals(2L, result.getDoctorId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void bookAppointment_PatientNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByIdAndRole(1L, Role.PATIENT)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(appointmentDto)
        );
        assertEquals("No Patient Exist with Id 1", exception.getMessage());
    }

    @Test
    void bookAppointment_WrongPatientId_ThrowsAccessDeniedException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        User otherPatient = User.builder().id(99L).email("other@test.com").role(Role.PATIENT).build();
        appointmentDto.setPatientId(99L);

        when(userRepository.findByIdAndRole(99L, Role.PATIENT)).thenReturn(Optional.of(otherPatient));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> appointmentService.bookAppointment(appointmentDto)
        );
        assertEquals("Patient can only book for himself", exception.getMessage());
    }

    @Test
    void bookAppointment_TimeInPast_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        appointmentDto.setAppointmentTime(LocalDateTime.now().minusDays(1));

        when(userRepository.findByIdAndRole(1L, Role.PATIENT)).thenReturn(Optional.of(patient));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.DOCTOR)).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.bookAppointment(appointmentDto)
        );
        assertEquals("Appointment must be in the future", exception.getMessage());
    }

    @Test
    void bookAppointment_OutsideWorkingHours_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        appointmentDto.setAppointmentTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));

        when(userRepository.findByIdAndRole(1L, Role.PATIENT)).thenReturn(Optional.of(patient));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.DOCTOR)).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.bookAppointment(appointmentDto)
        );
        assertEquals("Doctor works only between 09:00 and 21:00", exception.getMessage());
    }

    @Test
    void bookAppointment_ConflictingTime_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByIdAndRole(1L, Role.PATIENT)).thenReturn(Optional.of(patient));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.DOCTOR)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(1L);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.bookAppointment(appointmentDto)
        );
        assertEquals("Doctor already has an appointment at this time", exception.getMessage());
    }

    @Test
    void approveAppointment_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));
        appointment.setAppointmentTime(LocalDateTime.now().plusDays(1));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDto result = appointmentService.approveAppointment(1L);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).save(appointment);
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
    }

    @Test
    void approveAppointment_NotPending_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        appointment.setStatus(AppointmentStatus.BOOKED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.approveAppointment(1L)
        );
        assertEquals("Cannot approve appointment that is not PENDING", exception.getMessage());
    }

    @Test
    void approveAppointment_AppointmentInPast_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        appointment.setAppointmentTime(LocalDateTime.now().minusDays(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.approveAppointment(1L)
        );
        assertEquals("Cannot approve an appointment in the past", exception.getMessage());
    }

    @Test
    void completeAppointment_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setAppointmentTime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDto result = appointmentService.completeAppointment(1L);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).save(appointment);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void completeAppointment_NotBooked_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        appointment.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.completeAppointment(1L)
        );
        assertEquals("Can't complete a not BOOKED appointment", exception.getMessage());
    }

    @Test
    void completeAppointment_AppointmentInFuture_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setAppointmentTime(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.completeAppointment(1L)
        );
        assertEquals("Cannot mark an appointment as completed before its time", exception.getMessage());
    }

    @Test
    void cancelAppointment_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        appointmentService.cancelAppointment(1L);

        // Assert
        verify(appointmentRepository).save(appointment);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
    }

    @Test
    void cancelAppointment_AlreadyCancelled_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> appointmentService.cancelAppointment(1L)
        );
        assertEquals("Appointment cannot be cancelled (status = CANCELLED)", exception.getMessage());
    }

    @Test
    void getAllAppointmentsByPatient_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByPatient_Id(1L)).thenReturn(Arrays.asList(appointment));

        // Act
        List<AppointmentDto> result = appointmentService.getAllAppointmentsByPatient();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllAppointmentsByDoctor_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctor_Id(2L)).thenReturn(Arrays.asList(appointment));

        // Act
        List<AppointmentDto> result = appointmentService.getAllAppointmentsByDoctor();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateAppointment_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        AppointmentDto updateDto = new AppointmentDto();
        updateDto.setDoctorId(2L);
        updateDto.setAppointmentTime(
                LocalDateTime.of(
                        LocalDate.now().plusDays(2),
                        LocalTime.of(14, 0)
                )
        );

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsOverlappingAppointment(anyLong(), any(), any())).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // Act
        AppointmentDto result = appointmentService.updateAppointment(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).save(appointment);
    }
    @Test
    void updateAppointment_CompletedStatus_ThrowsIllegalArgumentException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        appointment.setStatus(AppointmentStatus.COMPLETED);
        AppointmentDto updateDto = new AppointmentDto();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.updateAppointment(1L, updateDto)
        );
        assertEquals("Only Pending or Booked appointments can be updated", exception.getMessage());
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}