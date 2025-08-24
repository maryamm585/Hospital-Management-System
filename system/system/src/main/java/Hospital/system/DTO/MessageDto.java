package Hospital.system.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class MessageDto {
    private Long senderId;
    private Long receiverId;

    @NotBlank
    @Size(min = 5, max = 1000)
    private String content;
}
