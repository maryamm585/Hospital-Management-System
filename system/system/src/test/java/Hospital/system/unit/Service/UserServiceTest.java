package Hospital.system.unit.Service;



import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Entity.User;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto userRegistrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setName("Test User");
        userRegistrationDto.setEmail("test@test.com");
        userRegistrationDto.setPassword("password123");
        userRegistrationDto.setRole("PATIENT");

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@test.com")
                .password("default123")
                .role(null)
                .build();
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.createUser(userRegistrationDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsIllegalArgumentException() {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(userRegistrationDto)
        );
        assertEquals("Email already exists!", exception.getMessage());
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    void getUserById_UserNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        jakarta.persistence.EntityNotFoundException exception = assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> userService.getUserById(1L)
        );
        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        jakarta.persistence.EntityNotFoundException exception = assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> userService.deleteUser(1L)
        );
        assertEquals("User not found with id: 1", exception.getMessage());
    }
}

