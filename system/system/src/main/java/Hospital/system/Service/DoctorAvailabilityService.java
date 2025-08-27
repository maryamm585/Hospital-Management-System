package Hospital.system.Service;

import Hospital.system.DTO.DoctorAvailabilityDto;
import Hospital.system.Entity.Appointment;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Repository.AppointmentRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    private static final LocalTime START_WORK = LocalTime.of(9, 0);
    private static final LocalTime END_WORK = LocalTime.of(21, 0);

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityDto> getAllDoctorsAvailability(LocalDate day) {
        List<User> doctors = userRepository.findByRole(Role.DOCTOR);
        List<DoctorAvailabilityDto> result = new ArrayList<>();

        for (User doctor : doctors) {
            result.add(getDoctorAvailabilityForDay(doctor, day));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityDto getDoctorAvailabilityById(Long doctorId, LocalDate day) {
        User doctor = userRepository.findByIdAndRole(doctorId, Role.DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        return getDoctorAvailabilityForDay(doctor, day);
    }

    private DoctorAvailabilityDto getDoctorAvailabilityForDay(User doctor, LocalDate day) {
        List<LocalDateTime> availableSlots = new ArrayList<>();

        LocalDateTime dayStart = day.atTime(START_WORK);
        LocalDateTime dayEnd = day.atTime(END_WORK);

        // fetch all appointments for this doctor on that day, excluding CANCELLED
        List<Appointment> appointments = appointmentRepository.findAppointmentsForDoctorExcludingStatus(
                doctor.getId(),
                AppointmentStatus.CANCELLED,
                dayStart,
                dayEnd
        );

        // generate all 1-hour slots
        LocalDateTime slotStart = dayStart;
        while (!slotStart.isAfter(dayEnd.minusHours(1))) {
            LocalDateTime slotEnd = slotStart.plusHours(1);

            LocalDateTime finalSlotStart = slotStart;
            boolean isBooked = appointments.stream()
                    .anyMatch(app -> app.getAppointmentTime().equals(finalSlotStart));

            // only add future and not booked slots
            if (!isBooked && slotStart.isAfter(LocalDateTime.now())) {
                availableSlots.add(slotStart);
            }

            slotStart = slotStart.plusHours(1);
        }

        return new DoctorAvailabilityDto(doctor.getId(), doctor.getName(), availableSlots);
    }

}
