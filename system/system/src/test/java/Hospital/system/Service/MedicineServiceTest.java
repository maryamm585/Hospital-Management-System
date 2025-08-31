package Hospital.system.Service;


import Hospital.system.DTO.MedicineDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.MedicineService;
import Hospital.system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicineService medicineService;

    private MedicineDto medicineDto;
    private Medicine medicine;
    private User pharmacy;

    @BeforeEach
    void setUp() {
        pharmacy = User.builder()
                .id(1L)
                .name("Test Pharmacy")
                .email("pharmacy@test.com")
                .role(Role.PHARMACY)
                .build();

        medicineDto = new MedicineDto();
        medicineDto.setName("Aspirin");
        medicineDto.setPrice(10.0);
        medicineDto.setStock(100);
        medicineDto.setPharmacyId(1L);

        medicine = Medicine.builder()
                .id(1L)
                .name("Aspirin")
                .price(10.0)
                .stock(100)
                .pharmacy(pharmacy)
                .build();
    }


    @Test
    void addMedicine_PharmacyNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> medicineService.addMedicine(medicineDto)
        );
        assertEquals("Pharmacy not found with id 1", exception.getMessage());
    }
    @Test
    void addMedicine_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);

        // Act
        MedicineDto result = medicineService.addMedicine(medicineDto);

        // Assert
        assertNotNull(result);
        assertEquals("Aspirin", result.getName());
        assertEquals(10.0, result.getPrice());
        assertEquals(100, result.getStock());
    }

    @Test
    void updateMedicine_Success_SamePharmacy() {
        // Arrange
        Long medicineId = 1L;

        // Set up the update DTO with new values
        medicineDto.setName("Updated Aspirin");
        medicineDto.setPrice(15.0);
        medicineDto.setStock(150);
        medicineDto.setPharmacyId(pharmacy.getId());

        // Stub repository calls
        when(medicineRepository.findById(medicineId)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MedicineDto result = medicineService.updateMedicine(medicineId, medicineDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Aspirin", result.getName());
        assertEquals(15.0, result.getPrice());
        assertEquals(150, result.getStock());
        assertEquals(pharmacy.getId(), result.getPharmacyId());

        // Verify the medicine was saved with updated values
        ArgumentCaptor<Medicine> captor = ArgumentCaptor.forClass(Medicine.class);
        verify(medicineRepository).save(captor.capture());

        Medicine savedMedicine = captor.getValue();
        assertEquals("Updated Aspirin", savedMedicine.getName());
        assertEquals(15.0, savedMedicine.getPrice());
        assertEquals(150, savedMedicine.getStock());
        assertEquals(pharmacy, savedMedicine.getPharmacy());

        // Verify repository calls
        verify(medicineRepository).findById(medicineId);
        verifyNoMoreInteractions(medicineRepository, userRepository);
    }




    @Test
    void updateMedicine_MedicineNotFound_ThrowsException() {
        // Arrange
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicineService.updateMedicine(99L, medicineDto));
    }

    @Test
    void updateMedicine_NewPharmacyNotFound_ThrowsException() {
        // Arrange
        medicineDto.setPharmacyId(2L);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicineService.updateMedicine(1L, medicineDto));
    }

    @Test
    void deleteMedicine_Success() {
        // Arrange
        when(medicineRepository.existsById(1L)).thenReturn(true);

        // Act
        medicineService.deleteMedicine(1L);

        // Assert
        verify(medicineRepository).deleteById(1L);
    }

    @Test
    void getMedicineById_Success() {
        // Arrange
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        // Act
        MedicineDto result = medicineService.getMedicineById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Aspirin", result.getName());
    }

    @Test
    void searchMedicinesByName_Success() {
        // Arrange
        List<Medicine> medicines = Arrays.asList(medicine);
        when(medicineRepository.findByNameContainingIgnoreCase("asp")).thenReturn(medicines);

        // Act
        List<MedicineDto> result = medicineService.searchMedicinesByName("asp");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aspirin", result.get(0).getName());
    }

}