package Hospital.system.Mapper;

import Hospital.system.DTO.MessageDto;
import Hospital.system.Entity.Message;
import Hospital.system.Entity.User;

public class MessageMapper {

    // dto to entity
    public static Message toEntity(MessageDto dto, User sender, User receiver) {
        if (dto == null) {
            return null;
        }

        return Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(dto.getContent())
                .build();
    }

    // entity to dto
    public static MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        MessageDto dto = new MessageDto();
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setContent(message.getContent());
        return dto;
    }
}
