package Hospital.system.Controller;

import Hospital.system.DTO.PrescriptionDto;
import Hospital.system.Service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")

class PrescriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    private PrescriptionDto samplePrescription;

    @BeforeEach
    void setUp() {
        samplePrescription = new PrescriptionDto();
        samplePrescription.setDoctorId(10L);
        samplePrescription.setPatientId(1L);
        samplePrescription.setMedicineName("Amoxicillin");
        samplePrescription.setDosage("500mg");
        samplePrescription.setInstructions("Take twice daily after meals");
    }

    @Test
    void testCreatePrescription() throws Exception {
        when(prescriptionService.createPrescription(any(PrescriptionDto.class)))
                .thenReturn(samplePrescription);

        mockMvc.perform(post("/api/prescriptions/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "doctorId": 10,
                                  "patientId": 1,
                                  "medicineName": "Amoxicillin",
                                  "dosage": "500mg",
                                  "instructions": "Take twice daily after meals"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(10))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.medicineName").value("Amoxicillin"))
                .andExpect(jsonPath("$.dosage").value("500mg"))
                .andExpect(jsonPath("$.instructions").value("Take twice daily after meals"));
    }

    @Test
    void testGetDoctorPrescriptions() throws Exception {
        when(prescriptionService.getDoctorPrescriptions())
                .thenReturn(List.of(samplePrescription));

        mockMvc.perform(get("/api/prescriptions/doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(10));
    }

    @Test
    void testGetPatientPrescriptions() throws Exception {
        when(prescriptionService.getPatientPrescriptions())
                .thenReturn(List.of(samplePrescription));

        mockMvc.perform(get("/api/prescriptions/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    @Test
    void testUpdatePrescription() throws Exception {
        PrescriptionDto updated = new PrescriptionDto();
        updated.setDoctorId(10L);
        updated.setPatientId(1L);
        updated.setMedicineName("Ibuprofen");
        updated.setDosage("200mg");
        updated.setInstructions("Take after meals if pain persists");

        when(prescriptionService.updatePrescription(eq(1L), any(PrescriptionDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/prescriptions/doctor/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "doctorId": 10,
                                  "patientId": 1,
                                  "medicineName": "Ibuprofen",
                                  "dosage": "200mg",
                                  "instructions": "Take after meals if pain persists"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicineName").value("Ibuprofen"))
                .andExpect(jsonPath("$.dosage").value("200mg"));
    }

    @Test
    void testDeletePrescription() throws Exception {
        doNothing().when(prescriptionService).deletePrescription(1L);

        mockMvc.perform(delete("/api/prescriptions/doctor/1"))
                .andExpect(status().isNoContent());
    }
}
