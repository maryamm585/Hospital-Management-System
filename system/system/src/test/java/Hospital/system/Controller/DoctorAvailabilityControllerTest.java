package Hospital.system.Controller;

import Hospital.system.DTO.DoctorAvailabilityDto;
import Hospital.system.Service.DoctorAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")

class DoctorAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorAvailabilityService availabilityService;

    @Test
    void testGetAllDoctorsAvailability() throws Exception {
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto(
                1L,
                "Dr. Ahmed",
                List.of(
                        LocalDateTime.of(2025, 9, 1, 10, 0),
                        LocalDateTime.of(2025, 9, 1, 11, 0)
                )
        );

        when(availabilityService.getAllDoctorsAvailability(any(LocalDate.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/patients/available-doctors")
                        .param("day", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(1L))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Ahmed"))
                .andExpect(jsonPath("$[0].availableTimes[0]").value("2025-09-01T10:00:00"))
                .andExpect(jsonPath("$[0].availableTimes[1]").value("2025-09-01T11:00:00"));
    }

    @Test
    void testGetDoctorAvailabilityById() throws Exception {
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto(
                5L,
                "Dr. Mona",
                List.of(
                        LocalDateTime.of(2025, 9, 1, 9, 0),
                        LocalDateTime.of(2025, 9, 1, 14, 0)
                )
        );

        when(availabilityService.getDoctorAvailabilityById(eq(5L), any(LocalDate.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/patients/available-doctors/{id}", 5L)
                        .param("day", "2025-09-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(5L))
                .andExpect(jsonPath("$.doctorName").value("Dr. Mona"))
                .andExpect(jsonPath("$.availableTimes[0]").value("2025-09-01T09:00:00"))
                .andExpect(jsonPath("$.availableTimes[1]").value("2025-09-01T14:00:00"));
    }
}
