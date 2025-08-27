package Hospital.system.Service;

import Hospital.system.DTO.LoginDto;
import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Security.JwtUtil;
import Hospital.system.Security.TokenBlacklistService;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public String registerUser(UserRegistrationDto registerUserDto) throws BadRequestException {
        if (userRepository.existsByEmail(registerUserDto.getEmail())) {
            throw new ValidationException("Email already in use");
        }

        User user = User.builder()
                .name(registerUserDto.getName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(parseRole(registerUserDto.getRole()))
                .build();

        User savedUser = userRepository.save(user);
        return jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
    }

    @Transactional
    public String login(LoginDto loginDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()));

            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public String logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No valid token provided");
        }

        String token = authHeader.substring(7);

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new IllegalStateException("Already logged out");
        }

        tokenBlacklistService.blacklistToken(token);
        return "Logged out successfully";
    }

    private Role parseRole(String roleString) throws BadRequestException {
        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid role: " + roleString + ". Must be one of: ADMIN, DOCTOR, PATIENT, PHARMACY");
        }
    }
}