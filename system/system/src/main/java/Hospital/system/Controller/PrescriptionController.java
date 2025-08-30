package Hospital.system.Controller;

import Hospital.system.DTO.PrescriptionDto;
import Hospital.system.Service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
//@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping("/doctor")
    public ResponseEntity<PrescriptionDto> createPrescription(@Valid @RequestBody PrescriptionDto prescriptionDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.createPrescription(prescriptionDto));
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<PrescriptionDto>> getDoctorPrescriptions(){
        return ResponseEntity.ok(prescriptionService.getDoctorPrescriptions());
    }
    @GetMapping("/patient")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptions(){
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions());
    }

    @PutMapping("/doctor/{id}")
    public ResponseEntity<PrescriptionDto> updatePrescription(@PathVariable Long id, @Valid @RequestBody PrescriptionDto updatedDto){
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, updatedDto));
    }

    @DeleteMapping("doctor/{id}")
    public ResponseEntity<?> deletePrescription(@PathVariable Long id){
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

}
