package Hospital.system.Controller;

import Hospital.system.DTO.AuthResponseDto;
import Hospital.system.DTO.LoginDto;
import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody UserRegistrationDto dto)
            throws BadRequestException {
        String token = authService.registerUser(dto);
        AuthResponseDto response = new AuthResponseDto(token, dto.getEmail(), dto.getRole(), "Registration successful");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        String token = authService.login(dto);
        AuthResponseDto response = new AuthResponseDto(token, dto.getEmail(),
                userRepository.findByEmail(dto.getEmail()).get().getRole().name(), "Login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDto> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String message = authService.logout(authHeader);
            AuthResponseDto response = new AuthResponseDto(null, null, null, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponseDto response = new AuthResponseDto(null, null, null, "Logout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}