package Hospital.system.Mapper;

import Hospital.system.DTO.OrderItemDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.Order;
import Hospital.system.Entity.OrderItem;

public class OrderItemMapper {

    // dto to entity
    public static OrderItem toEntity(OrderItemDto dto, Order order, Medicine medicine) {
        return OrderItem.builder()
                .order(order)            // link to parent order
                .medicine(medicine)      // looked up in service from medicineId
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build();
    }

    // entity to dto
    public static OrderItemDto toDto(OrderItem entity) {
        OrderItemDto dto = new OrderItemDto(
                entity.getMedicine().getId(),
                entity.getQuantity(),
                entity.getPrice()
        );
        return dto;
    }
}
