package Hospital.system.unit.Service;


import Hospital.system.DTO.DoctorAvailabilityDto;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.AppointmentRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.DoctorAvailabilityService;
import Hospital.system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorAvailabilityService doctorAvailabilityService;

    private User doctor;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        doctor = User.builder()
                .id(1L)
                .name("Dr. Test")
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .build();

        testDate = LocalDate.now().plusDays(1);
    }

    @Test
    void getDoctorAvailabilityById_Success() {
        // Arrange
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findAppointmentsForDoctorExcludingStatus(
                eq(1L), eq(AppointmentStatus.CANCELLED), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        DoctorAvailabilityDto result = doctorAvailabilityService.getDoctorAvailabilityById(1L, testDate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDoctorId());
        assertEquals("Dr. Test", result.getDoctorName());
        assertNotNull(result.getAvailableTimes());
    }

    @Test
    void getDoctorAvailabilityById_DoctorNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> doctorAvailabilityService.getDoctorAvailabilityById(1L, testDate)
        );
        assertEquals("Doctor not found with id 1", exception.getMessage());
    }

    @Test
    void getAllDoctorsAvailability_Success() {
        // Arrange
        List<User> doctors = Arrays.asList(doctor);
        when(userRepository.findByRole(Role.DOCTOR)).thenReturn(doctors);
        when(appointmentRepository.findAppointmentsForDoctorExcludingStatus(
                eq(1L), eq(AppointmentStatus.CANCELLED), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        List<DoctorAvailabilityDto> result = doctorAvailabilityService.getAllDoctorsAvailability(testDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getDoctorId());
    }
}