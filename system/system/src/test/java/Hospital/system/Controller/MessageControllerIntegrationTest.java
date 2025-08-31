package Hospital.system.Controller;

import Hospital.system.DTO.MessageDto;
import Hospital.system.Service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")

class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    private MessageDto sampleMessage;

    @BeforeEach
    void setUp() {
        sampleMessage = new MessageDto();
        sampleMessage.setSenderId(10L);
        sampleMessage.setReceiverId(20L);
        sampleMessage.setContent("Hello Patient");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testSendMessage() throws Exception {
        Mockito.when(messageService.sendMessage(any(MessageDto.class)))
                .thenReturn(sampleMessage);

        mockMvc.perform(post("/api/messages")
                        .contentType("application/json")
                        .content("""
                                {"senderId":10,"receiverId":20,"content":"Hello Patient"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderId").value(10))
                .andExpect(jsonPath("$.receiverId").value(20))
                .andExpect(jsonPath("$.content").value("Hello Patient"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetMessagesByUser() throws Exception {
        Mockito.when(messageService.getMessagesByUser(20L))
                .thenReturn(List.of(sampleMessage));

        mockMvc.perform(get("/api/messages/user/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value(10))
                .andExpect(jsonPath("$[0].receiverId").value(20))
                .andExpect(jsonPath("$[0].content").value("Hello Patient"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void testGetConversation() throws Exception {
        MessageDto reply = new MessageDto();
        reply.setSenderId(20L);
        reply.setReceiverId(10L);
        reply.setContent("I’m fine");

        Mockito.when(messageService.getConversation(10L, 20L))
                .thenReturn(List.of(sampleMessage, reply));

        mockMvc.perform(get("/api/messages/conversation")
                        .param("senderId", "10")
                        .param("receiverId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello Patient"))
                .andExpect(jsonPath("$[1].content").value("I’m fine"));
    }
}
