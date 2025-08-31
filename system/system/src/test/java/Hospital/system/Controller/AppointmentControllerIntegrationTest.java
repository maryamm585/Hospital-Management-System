package Hospital.system.Controller;

import Hospital.system.DTO.AppointmentDto;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")

class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private AppointmentDto sampleDto() {
        AppointmentDto dto = new AppointmentDto();
        dto.setDoctorId(10L);
        dto.setPatientId(20L);
        dto.setAppointmentTime(LocalDateTime.now().plusDays(1));
        dto.setStatus(String.valueOf(AppointmentStatus.PENDING));
        return dto;
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testBookAppointment() throws Exception {
        AppointmentDto dto = sampleDto();
        when(appointmentService.bookAppointment(any(AppointmentDto.class))).thenReturn(dto);

        mockMvc.perform(post("/api/appointments/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(dto.getPatientId()))
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetAllAppointmentsByPatient() throws Exception {
        AppointmentDto dto = sampleDto();
        when(appointmentService.getAllAppointmentsByPatient()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(dto.getPatientId()));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetAllAppointmentsByPatientAndStatus() throws Exception {
        AppointmentDto dto = sampleDto();
        when(appointmentService.getAllAppointmentsByPatientAndStatus(eq(AppointmentStatus.PENDING)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments/patient/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetAllAppointmentsByDoctor() throws Exception {
        AppointmentDto dto = sampleDto();
        when(appointmentService.getAllAppointmentsByDoctor()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/appointments/doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(dto.getDoctorId()));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testApproveAppointment() throws Exception {
        AppointmentDto dto = sampleDto();
        dto.setStatus(String.valueOf(AppointmentStatus.COMPLETED));
        when(appointmentService.approveAppointment(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/appointments/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testCancelAppointment() throws Exception {
        mockMvc.perform(patch("/api/appointments/1/cancel"))
                .andExpect(status().isNoContent());
    }
}
