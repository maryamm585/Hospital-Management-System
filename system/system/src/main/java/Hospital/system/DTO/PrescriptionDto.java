package Hospital.system.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrescriptionDto {
    private Long doctorId;
    private Long patientId;
    private String medicineName;

    @NotBlank
    private String dosage;

    @NotBlank
    private String instructions;
}
