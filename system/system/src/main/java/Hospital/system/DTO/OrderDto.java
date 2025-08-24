package Hospital.system.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto {
    @NotNull
    private Long patientId;

    @NotNull
    @Min(1)
    private Double totalPrice;
    private String status;

    @NotEmpty
    private List<OrderItemDto> items;
}
