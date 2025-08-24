package Hospital.system.Mapper;

import Hospital.system.DTO.AppointmentDto;
import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.User;

public class AppointmentMapper {

    //dto to entity
    public static Appointment toEntity(AppointmentDto dto, User doctor, User patient) {
        return Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentTime(dto.getAppointmentTime())
                .status(dto.getStatus() != null
                        ? AppointmentStatus.valueOf(dto.getStatus().toUpperCase())
                        : AppointmentStatus.PENDING)
                .build();
    }

    // entity to dto
    public static AppointmentDto toDto(Appointment entity) {
        AppointmentDto dto = new AppointmentDto();
        dto.setDoctorId(entity.getDoctor().getId());
        dto.setPatientId(entity.getPatient().getId());
        dto.setAppointmentTime(entity.getAppointmentTime());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}
