package Hospital.system.Service;

import Hospital.system.DTO.LoginDto;
import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Security.JwtUtil;
import Hospital.system.Security.TokenBlacklistService;
import Hospital.system.Service.AuthService;
import Hospital.system.exception.ValidationException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationDto registrationDto;
    private LoginDto loginDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setName("Test User");
        registrationDto.setEmail("test@test.com");
        registrationDto.setPassword("password123");
        registrationDto.setRole("PATIENT");

        loginDto = new LoginDto();
        loginDto.setEmail("test@test.com");
        loginDto.setPassword("password123");

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.PATIENT)
                .build();
    }

    @Test
    void registerUser_Success() throws BadRequestException {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("test@test.com", "PATIENT")).thenReturn("jwt-token");

        // Act
        String result = authService.registerUser(registrationDto);

        // Assert
        assertEquals("jwt-token", result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsValidationException() {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.registerUser(registrationDto)
        );
        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_InvalidRole_ThrowsBadRequestException() {
        // Arrange
        registrationDto.setRole("INVALID_ROLE");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

        // Act & Assert
        assertThrows(
                BadRequestException.class,
                () -> authService.registerUser(registrationDto)
        );
        verify(userRepository, never()).save(any(User.class));
    }



    @Test
    void login_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("test@test.com", "PATIENT")).thenReturn("jwt-token");

        // Act
        String result = authService.login(loginDto);

        // Assert
        assertEquals("jwt-token", result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginDto)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_UserNotFoundAfterAuthentication_ThrowsBadCredentialsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginDto)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_AuthenticationManagerThrowsGenericException_ThrowsBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginDto)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void logout_Success() {
        // Arrange
        String authHeader = "Bearer jwt-token";
        when(tokenBlacklistService.isTokenBlacklisted("jwt-token")).thenReturn(false);

        // Act
        String result = authService.logout(authHeader);

        // Assert
        assertEquals("Logged out successfully", result);
        verify(tokenBlacklistService).blacklistToken("jwt-token");
        verify(tokenBlacklistService).isTokenBlacklisted("jwt-token");
    }

    @Test
    void logout_AlreadyLoggedOut_ThrowsIllegalStateException() {
        // Arrange
        String authHeader = "Bearer jwt-token";
        when(tokenBlacklistService.isTokenBlacklisted("jwt-token")).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.logout(authHeader)
        );
        assertEquals("Already logged out", exception.getMessage());
        verify(tokenBlacklistService, never()).blacklistToken("jwt-token");
    }

    @Test
    void logout_InvalidAuthHeader_ThrowsIllegalArgumentException() {
        // Test null header
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.logout(null)
        );
        assertEquals("No valid token provided", exception.getMessage());

        // Test header without Bearer prefix
        exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.logout("jwt-token")
        );
        assertEquals("No valid token provided", exception.getMessage());

        // Test empty header
        exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.logout("")
        );
        assertEquals("No valid token provided", exception.getMessage());
    }


}