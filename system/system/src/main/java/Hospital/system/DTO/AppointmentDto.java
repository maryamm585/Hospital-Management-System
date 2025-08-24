package Hospital.system.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    @NotNull
    private Long doctorId;
    @NotNull
    private Long patientId;
    @NotNull
    private LocalDateTime appointmentTime;
    private String status;
}
