package Hospital.system.Controller;

import Hospital.system.DTO.DoctorAvailabilityDto;
import Hospital.system.Service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    // GET /api/patient/available-doctors?day=2025-09-01
    @GetMapping("/available-doctors")
    public List<DoctorAvailabilityDto> getAllDoctorsAvailability(
            @RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return availabilityService.getAllDoctorsAvailability(day);
    }

    // GET /api/patient/available-doctors/{id}?day=2025-09-01
    @GetMapping("/available-doctors/{id}")
    public DoctorAvailabilityDto getDoctorAvailability(
            @PathVariable("id") Long doctorId,
            @RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return availabilityService.getDoctorAvailabilityById(doctorId, day);
    }
}
