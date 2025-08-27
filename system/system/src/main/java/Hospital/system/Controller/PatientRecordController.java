package Hospital.system.Controller;

import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Service.PatientRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient-records")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class PatientRecordController {

    private final PatientRecordService patientRecordService;

    @PostMapping
    public ResponseEntity<PatientRecordDto> createRecord(@RequestBody PatientRecordDto dto) {
        return ResponseEntity.ok(patientRecordService.createRecord(dto));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientRecordDto>> getRecordsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientRecordService.getRecordsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PatientRecordDto>> getRecordsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(patientRecordService.getRecordsByDoctor(doctorId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientRecordDto>> searchRecords(@RequestParam String keyword) {
        return ResponseEntity.ok(patientRecordService.searchRecordsByNotes(keyword));
    }
}
