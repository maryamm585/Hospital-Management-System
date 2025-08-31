package Hospital.system.Controller;

import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Service.PatientRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")

class PatientRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientRecordService patientRecordService;

    private PatientRecordDto sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new PatientRecordDto();
        sampleRecord.setPatientId(1L);
        sampleRecord.setDoctorId(100L);
        sampleRecord.setNotes("Patient shows improvement after medication.");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testCreateRecord() throws Exception {
        when(patientRecordService.createRecord(any(PatientRecordDto.class)))
                .thenReturn(sampleRecord);

        mockMvc.perform(post("/api/patient-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 1,
                                  "doctorId": 100,
                                  "notes": "Patient shows improvement after medication."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(100))
                .andExpect(jsonPath("$.notes").value("Patient shows improvement after medication."));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetRecordsByPatient() throws Exception {
        when(patientRecordService.getRecordsByPatient(1L))
                .thenReturn(List.of(sampleRecord));

        mockMvc.perform(get("/api/patient-records/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetRecordsByDoctor() throws Exception {
        when(patientRecordService.getRecordsByDoctor(100L))
                .thenReturn(List.of(sampleRecord));

        mockMvc.perform(get("/api/patient-records/doctor/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(100));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testSearchRecords() throws Exception {
        when(patientRecordService.searchRecordsByNotes("improvement"))
                .thenReturn(List.of(sampleRecord));

        mockMvc.perform(get("/api/patient-records/search")
                        .param("keyword", "improvement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notes").value("Patient shows improvement after medication."));
    }
}
