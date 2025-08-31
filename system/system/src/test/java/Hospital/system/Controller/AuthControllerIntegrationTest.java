package Hospital.system.Controller;

import Hospital.system.DTO.LoginDto;
import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Test
    void testRegisterUser() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");
        dto.setRole("PATIENT");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(dto.getEmail()))
                .andExpect(jsonPath("$.role").value(dto.getRole()))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Transactional
    @Test
    void testLoginUser() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Jane Doe");
        dto.setEmail("jane@example.com");
        dto.setPassword("password123");
        dto.setRole("PATIENT");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(dto.getEmail());
        loginDto.setPassword(dto.getPassword());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(loginDto.getEmail()))
                .andExpect(jsonPath("$.role").value(dto.getRole()))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Transactional
    @Test
    void testLogoutUser() throws Exception {
        UserRegistrationDto registerDto = new UserRegistrationDto();
        registerDto.setName("Alice");
        registerDto.setEmail("alice@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole("PATIENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(registerDto.getEmail());
        loginDto.setPassword(registerDto.getPassword());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Transactional
    @Test
    void testRegisterUserWithInvalidRole() throws Exception {
        UserRegistrationDto invalidRoleDto = new UserRegistrationDto();
        invalidRoleDto.setName("Bob");
        invalidRoleDto.setEmail("bob@example.com");
        invalidRoleDto.setPassword("password123");
        invalidRoleDto.setRole("INVALID_ROLE");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRoleDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }


}
