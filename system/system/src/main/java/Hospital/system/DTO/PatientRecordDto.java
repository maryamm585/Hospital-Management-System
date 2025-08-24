package Hospital.system.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientRecordDto {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotBlank
    @Size(min = 5, max = 1000)
    private String notes;
}
