package Hospital.system.Service;

import Hospital.system.DTO.MessageDto;
import Hospital.system.Entity.Message;
import Hospital.system.Entity.User;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.Mapper.MessageMapper;
import Hospital.system.Repository.MessageRepository;
import Hospital.system.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto sendMessage(MessageDto dto) {
        log.debug("Attempting to send message from senderId={} to receiverId={}", dto.getSenderId(), dto.getReceiverId());

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> {
                    log.error("Sender not found with id={}", dto.getSenderId());
                    return new ResourceNotFoundException("Sender not found with id " + dto.getSenderId());
                });

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> {
                    log.error("Receiver not found with id={}", dto.getReceiverId());
                    return new ResourceNotFoundException("Receiver not found with id " + dto.getReceiverId());
                });
        Message message = MessageMapper.toEntity(dto, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        log.info("Message sent successfully: messageId={}, senderId={}, receiverId={}",
                savedMessage.getId(), savedMessage.getSender().getId(), savedMessage.getReceiver().getId());

        return MessageMapper.toDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByUser(Long userId) {
        log.debug("Fetching messages for userId={}", userId);

        List<Message> sent = messageRepository.findBySender_Id(userId);
        List<Message> received = messageRepository.findByReceiver_Id(userId);

        List<MessageDto> messages = List.of(sent, received).stream()
                .flatMap(List::stream)
                .sorted((m1, m2) -> m1.getTime().compareTo(m2.getTime()))
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());

        log.info("Fetched {} total messages for userId={}", messages.size(), userId);
        return messages;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(Long senderId, Long receiverId) {
        log.debug("Fetching conversation between senderId={} and receiverId={}", senderId, receiverId);

        List<Message> messages = messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(senderId, receiverId);
        messages.addAll(messageRepository.findBySender_IdAndReceiver_IdOrderByTimeAsc(receiverId, senderId));

        List<MessageDto> conversation = messages.stream()
                .sorted((m1, m2) -> m1.getTime().compareTo(m2.getTime()))
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
        log.info("Fetched {} messages in conversation between senderId={} and receiverId={}",
                conversation.size(), senderId, receiverId);
        return conversation;
    }
}
