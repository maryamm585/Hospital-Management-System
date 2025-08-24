package Hospital.system.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MedicineDto {
    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Double price;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    private Long pharmacyId;
}
