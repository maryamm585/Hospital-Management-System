package Hospital.system.Mapper;

import Hospital.system.DTO.OrderDto;
import Hospital.system.DTO.OrderItemDto;
import Hospital.system.Entity.Order;
import Hospital.system.Entity.OrderItem;
import Hospital.system.Entity.User;
import Hospital.system.Entity.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    // Convert DTO -> Entity
    public static Order toEntity(OrderDto dto, User patient,User pharmacy, List<OrderItem> items) {
        return Order.builder()
                .patient(patient) // comes from service (UserRepository.findById)
                .pharmacy(pharmacy)
                .totalPrice(dto.getTotalPrice())
                .status(dto.getStatus() != null
                        ? OrderStatus.valueOf(dto.getStatus())
                        : OrderStatus.PLACED) // default if null
                .items(items) // mapped separately
                .build();
    }

    // Convert Entity -> DTO
    public static OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setPatientId(order.getPatient().getId());
        dto.setPharmacyId(order.getPharmacy().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(OrderItemMapper::toDto) // delegate mapping to OrderItemMapper
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }
}
