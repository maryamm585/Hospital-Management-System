package Hospital.system.Service;

import Hospital.system.DTO.MessageDto;
import Hospital.system.Entity.Message;
import Hospital.system.Entity.User;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.Mapper.MessageMapper;
import Hospital.system.Repository.MessageRepository;
import Hospital.system.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto sendMessage(MessageDto dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id " + dto.getSenderId()));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id " + dto.getReceiverId()));

        Message message = MessageMapper.toEntity(dto, sender, receiver);
        Message savedMessage = messageRepository.save(message);

        return MessageMapper.toDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByUser(Long userId) {
        List<Message> sent = messageRepository.findBySender_Id(userId);
        List<Message> received = messageRepository.findByReceiver_Id(userId);

        return List.of(sent, received).stream()
                .flatMap(List::stream)
                .sorted((m1, m2) -> m1.getTime().compareTo(m2.getTime()))
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(Long senderId, Long receiverId) {
        List<Message> messages = messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(senderId, receiverId);
        messages.addAll(messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(receiverId, senderId));

        return messages.stream()
                .sorted((m1, m2) -> m1.getTime().compareTo(m2.getTime()))
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
    }
}
