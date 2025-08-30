package Hospital.system.unit.Service;


import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Entity.PatientRecord;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.PatientRecordRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.PatientRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientRecordServiceTest {
    @Mock
    private PatientRecordRepository patientRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientRecordService patientRecordService;

    private PatientRecordDto patientRecordDto;
    private PatientRecord patientRecord;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(1L)
                .name("Patient")
                .email("patient@test.com")
                .role(Role.PATIENT)
                .build();

        doctor = User.builder()
                .id(2L)
                .name("Doctor")
                .email("doctor@test.com")
                .role(Role.DOCTOR)
                .build();

        patientRecordDto = new PatientRecordDto();
        patientRecordDto.setPatientId(1L);
        patientRecordDto.setDoctorId(2L);
        patientRecordDto.setNotes("Patient consultation notes");

        patientRecord = PatientRecord.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .notes("Patient consultation notes")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createRecord_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(patientRecordRepository.save(any(PatientRecord.class))).thenReturn(patientRecord);

        // Act
        PatientRecordDto result = patientRecordService.createRecord(patientRecordDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals(2L, result.getDoctorId());
        assertEquals("Patient consultation notes", result.getNotes());
    }

    @Test
    void createRecord_PatientNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> patientRecordService.createRecord(patientRecordDto)
        );
        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void getRecordsByPatient_Success() {
        // Arrange
        when(patientRecordRepository.findByPatient_Id(1L)).thenReturn(Arrays.asList(patientRecord));

        // Act
        List<PatientRecordDto> result = patientRecordService.getRecordsByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Patient consultation notes", result.get(0).getNotes());
    }

    @Test
    void getRecordsByDoctor_Success() {
        // Arrange
        when(patientRecordRepository.findByDoctor_Id(2L)).thenReturn(Arrays.asList(patientRecord));

        // Act
        List<PatientRecordDto> result = patientRecordService.getRecordsByDoctor(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchRecordsByNotes_Success() {
        // Arrange
        when(patientRecordRepository.searchByNotes("consultation")).thenReturn(Arrays.asList(patientRecord));

        // Act
        List<PatientRecordDto> result = patientRecordService.searchRecordsByNotes("consultation");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}