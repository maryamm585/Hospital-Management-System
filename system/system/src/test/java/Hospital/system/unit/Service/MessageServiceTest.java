package Hospital.system.unit.Service;


import Hospital.system.DTO.MessageDto;
import Hospital.system.Entity.Message;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.MessageRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.MessageService;
import Hospital.system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private MessageDto messageDto;
    private Message message;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .name("Sender")
                .email("sender@test.com")
                .role(Role.PATIENT)
                .build();

        receiver = User.builder()
                .id(2L)
                .name("Receiver")
                .email("receiver@test.com")
                .role(Role.DOCTOR)
                .build();

        messageDto = new MessageDto();
        messageDto.setSenderId(1L);
        messageDto.setReceiverId(2L);
        messageDto.setContent("Test message content");

        message = Message.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .content("Test message content")
                .time(LocalDateTime.now())
                .build();
    }

    @Test
    void sendMessage_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // Act
        MessageDto result = messageService.sendMessage(messageDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals("Test message content", result.getContent());
    }

    @Test
    void sendMessage_SenderNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> messageService.sendMessage(messageDto)
        );
        assertEquals("Sender not found with id 1", exception.getMessage());
    }

    @Test
    void getMessagesByUser_Success() {
        // Arrange
        List<Message> sentMessages = Arrays.asList(message);
        List<Message> receivedMessages = Arrays.asList();

        when(messageRepository.findBySender_Id(1L)).thenReturn(sentMessages);
        when(messageRepository.findByReceiver_Id(1L)).thenReturn(receivedMessages);

        // Act
        List<MessageDto> result = messageService.getMessagesByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getConversation_Success() {
        // Arrange
        List<Message> messages1 = Arrays.asList(message);
        List<Message> messages2 = Arrays.asList();

        when(messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(1L, 2L)).thenReturn(messages1);
        when(messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(2L, 1L)).thenReturn(messages2);

        // Act
        List<MessageDto> result = messageService.getConversation(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}