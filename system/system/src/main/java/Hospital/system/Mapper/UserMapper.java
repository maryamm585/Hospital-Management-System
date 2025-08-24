package Hospital.system.Mapper;

import Hospital.system.DTO.UserRegistrationDto;
import Hospital.system.Entity.User;

public class UserMapper {
    // dto to entity
    public static User toEntity(UserRegistrationDto dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(null)
                .role(null)
                .build();
    }

    // entity to dto
    public static UserRegistrationDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
