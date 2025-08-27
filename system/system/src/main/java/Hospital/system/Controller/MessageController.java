package Hospital.system.Controller;

import Hospital.system.DTO.MessageDto;
import Hospital.system.Service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody MessageDto dto) {
        return ResponseEntity.ok(messageService.sendMessage(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageDto>> getMessagesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getMessagesByUser(userId));
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDto>> getConversation(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        return ResponseEntity.ok(messageService.getConversation(senderId, receiverId));
    }
}
