package Hospital.system.Mapper;

import Hospital.system.DTO.PrescriptionDto;
import Hospital.system.Entity.Prescription;
import Hospital.system.Entity.User;
import Hospital.system.Entity.Medicine;

//note:
// the dto only carries IDs So, when converting dto to entity your service layer
// must fetch the User doctor, User patient, and Medicine medicine objects from the DB first
//.findById
// except medicine, won't deal with ids for it.

public class PrescriptionMapper {
    // dto to entity
    public static Prescription toEntity(PrescriptionDto dto, User doctor, User patient, Medicine medicine) {
        return Prescription.builder()
                .doctor(doctor)
                .patient(patient)
                .medicine(medicine)
                .dosage(dto.getDosage())
                .instructions(dto.getInstructions())
                .build();
    }

    // entity to dto
    public static PrescriptionDto toDto(Prescription entity) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setDoctorId(entity.getDoctor().getId());
        dto.setPatientId(entity.getPatient().getId());
        dto.setMedicineName(entity.getMedicine().getName());
        dto.setDosage(entity.getDosage());
        dto.setInstructions(entity.getInstructions());
        return dto;
    }
}
