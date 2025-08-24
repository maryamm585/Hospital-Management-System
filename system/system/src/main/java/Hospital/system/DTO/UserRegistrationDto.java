package Hospital.system.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank
    @Size(min = 2, max = 20)
    private String name;

    @NotBlank
    @Email
    private String email;
}
