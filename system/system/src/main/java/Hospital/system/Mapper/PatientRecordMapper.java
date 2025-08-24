package Hospital.system.Mapper;


import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Entity.PatientRecord;
import Hospital.system.Entity.User;

public class PatientRecordMapper {

    // dto to entity
    public static PatientRecord toEntity(PatientRecordDto dto, User patient, User doctor) {
        return PatientRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .notes(dto.getNotes())
                .build();
    }

    // entity to dto
    public static PatientRecordDto toDto(PatientRecord entity) {
        PatientRecordDto dto = new PatientRecordDto();
        dto.setPatientId(entity.getPatient().getId());
        dto.setDoctorId(entity.getDoctor().getId());
        dto.setNotes(entity.getNotes());
        return dto;
    }
}
