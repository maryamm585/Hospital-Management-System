package Hospital.system.Controller;

import Hospital.system.DTO.MedicineDto;
import Hospital.system.Service.MedicineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")
class MedicineControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicineService medicineService;

    @Test
    void testAddMedicine() throws Exception {
        MedicineDto dto = new MedicineDto();
        dto.setName("Panadol");
        dto.setPrice(10.0);
        dto.setStock(50);
        dto.setPharmacyId(1L);


        Mockito.when(medicineService.addMedicine(any(MedicineDto.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/medicines")
                        .contentType("application/json")
                        .content("""
                                {"name":"Panadol","stock":50,"price":10.0,"pharmacyId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Panadol"))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.pharmacyId").value(1));
    }

    @Test
    void testGetMedicinesByPharmacy() throws Exception {
        MedicineDto dto = new MedicineDto();
        dto.setName("Panadol");
        dto.setPrice(10.00);
        dto.setStock(50);
        dto.setPharmacyId(2L);
        Mockito.when(medicineService.getMedicinesByPharmacy(2L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/medicines/pharmacy/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Panadol"))
                .andExpect(jsonPath("$[0].pharmacyId").value(2));
    }

    @Test
    void testSearchMedicines() throws Exception {
        MedicineDto dto = new MedicineDto();
        dto.setName("Panadol");
        dto.setPrice(10.0);
        dto.setStock(50);
        dto.setPharmacyId(1L);

        Mockito.when(medicineService.searchMedicinesByName("Pan"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/medicines/search")
                        .param("name", "Pan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Panadol"));
    }

    @Test
    void testGetAvailableMedicines() throws Exception {
        MedicineDto dto = new MedicineDto();
        dto.setName("Aspirin");
        dto.setPrice(5.0);
        dto.setStock(100);
        dto.setPharmacyId(1L);
        Mockito.when(medicineService.getAvailableMedicines())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/medicines/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aspirin"))
                .andExpect(jsonPath("$[0].stock").value(100));
    }
}
