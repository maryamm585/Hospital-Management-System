package Hospital.system.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 7)
    private String password;
}
