package Hospital.system.unit.Service;


import Hospital.system.DTO.PrescriptionDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.Prescription;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.PrescriptionRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.PrescriptionService;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private PrescriptionDto prescriptionDto;
    private Prescription prescription;
    private User doctor;
    private User patient;
    private Medicine medicine;

    @BeforeEach
    void setUp() {
        doctor = User.builder()
                .id(1L)
                .name("Doctor")
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .build();

        patient = User.builder()
                .id(2L)
                .name("Patient")
                .email("patient@test.com")
                .role(Role.PATIENT)
                .build();

        medicine = Medicine.builder()
                .id(1L)
                .name("Aspirin")
                .price(10.0)
                .stock(100)
                .build();

        prescriptionDto = new PrescriptionDto();
        prescriptionDto.setDoctorId(1L);
        prescriptionDto.setPatientId(2L);
        prescriptionDto.setMedicineName("Aspirin");
        prescriptionDto.setDosage("100mg twice daily");
        prescriptionDto.setInstructions("Take with food");

        prescription = Prescription.builder()
                .id(1L)
                .doctor(doctor)
                .patient(patient)
                .medicine(medicine)
                .dosage("100mg twice daily")
                .instructions("Take with food")
                .build();
    }

    @Test
    void createPrescription_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.of(doctor));
        when(userRepository.findByIdAndRole(2L, Role.PATIENT)).thenReturn(Optional.of(patient));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        // Act
        PrescriptionDto result = prescriptionService.createPrescription(prescriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDoctorId());
        assertEquals(2L, result.getPatientId());
        assertEquals("Aspirin", result.getMedicineName());
    }

    @Test
    void createPrescription_WrongDoctorEmail_ThrowsAccessDeniedException() {
        // Arrange
        mockSecurityContext("otherdoctor@test.com");
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.of(doctor));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> prescriptionService.createPrescription(prescriptionDto)
        );
        assertEquals("You can't prescribe using another doctor's Id", exception.getMessage());
    }

    @Test
    void createPrescription_DoctorNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> prescriptionService.createPrescription(prescriptionDto)
        );
        assertEquals("Doctor not found with id 1", exception.getMessage());
    }

    @Test
    void getPatientPrescriptions_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(prescriptionRepository.findByPatient_Id(2L)).thenReturn(Arrays.asList(prescription));

        // Act
        List<PrescriptionDto> result = prescriptionService.getPatientPrescriptions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updatePrescription_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.of(doctor));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        // Act
        PrescriptionDto result = prescriptionService.updatePrescription(1L, prescriptionDto);

        // Assert
        assertNotNull(result);
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void updatePrescription_ChangePatientId_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        prescriptionDto.setPatientId(99L); // Different patient ID

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(userRepository.findByIdAndRole(1L, Role.DOCTOR)).thenReturn(Optional.of(doctor));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> prescriptionService.updatePrescription(1L, prescriptionDto)
        );
        assertEquals("Patient ID cannot be changed", exception.getMessage());
    }

    @Test
    void deletePrescription_Success() {
        // Arrange
        mockSecurityContext("doctor@test.com");
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(doctor));

        // Act
        prescriptionService.deletePrescription(1L);

        // Assert
        verify(prescriptionRepository).delete(prescription);
    }

    @Test
    void deletePrescription_WrongDoctor_ThrowsAccessDeniedException() {
        // Arrange
        User otherDoctor = User.builder().id(99L).email("other@test.com").role(Role.DOCTOR).build();
        mockSecurityContext("other@test.com");

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherDoctor));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> prescriptionService.deletePrescription(1L)
        );
        assertEquals("Only the doctor who created the prescription can delete it", exception.getMessage());
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}