package Hospital.system.Mapper;

import Hospital.system.DTO.MedicineDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.User;

public class MedicineMapper {

    // dto to entity
    public static Medicine toEntity(MedicineDto dto, User pharmacy) {
        if (dto == null) {
            return null;
        }

        return Medicine.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .pharmacy(pharmacy) // we pass pharmacy (User entity) from service layer
                .build();
    }

    // entity to dto
    public static MedicineDto toDto(Medicine medicine) {
        if (medicine == null) {
            return null;
        }

        MedicineDto dto = new MedicineDto();
        dto.setName(medicine.getName());
        dto.setPrice(medicine.getPrice());
        dto.setStock(medicine.getStock());
        dto.setPharmacyId(
                medicine.getPharmacy() != null ? medicine.getPharmacy().getId() : null
        );

        return dto;
    }
}
