package Hospital.system.Controller;

import Hospital.system.DTO.AppointmentDto;
import Hospital.system.Entity.AppointmentStatus;
import Hospital.system.Service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping("/patient")
    public ResponseEntity<AppointmentDto> bookAppointment(@Valid @RequestBody AppointmentDto appointmentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(appointmentDto));

    }

    @GetMapping("/patient")
    public ResponseEntity<List<AppointmentDto>> getAllAppointmentsByPatient(){
        return ResponseEntity.ok(appointmentService.getAllAppointmentsByPatient());
    }

    @GetMapping("/patient/status/{status}")
    public ResponseEntity<List<AppointmentDto>> getAllAppointmentsByPatientAndStatus(@PathVariable AppointmentStatus status){
        return ResponseEntity.ok(appointmentService.getAllAppointmentsByPatientAndStatus(status));
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentDto>> getAllAppointmentsByDoctor(){
        return ResponseEntity.ok(appointmentService.getAllAppointmentsByDoctor());
    }

    @GetMapping("/doctor/status/{status}")
    public ResponseEntity<List<AppointmentDto>> getAllAppointmentsByDoctorAndStatus(@PathVariable AppointmentStatus status){
        return ResponseEntity.ok(appointmentService.getAllAppointmentsByDoctorAndStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentDto updatedDto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, updatedDto));
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<AppointmentDto> approveAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.approveAppointment(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentDto> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

}
