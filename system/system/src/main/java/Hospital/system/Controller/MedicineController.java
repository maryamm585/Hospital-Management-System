package Hospital.system.Controller;

import Hospital.system.DTO.MedicineDto;
import Hospital.system.Service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@PreAuthorize("hasRole('PHARMACY')")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    public ResponseEntity<MedicineDto> addMedicine(@Valid @RequestBody MedicineDto dto) {
        return ResponseEntity.ok(medicineService.addMedicine(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineDto> updateMedicine(@PathVariable Long id, @Valid @RequestBody MedicineDto dto) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<MedicineDto>> getMedicinesByPharmacy(@PathVariable Long pharmacyId) {
        return ResponseEntity.ok(medicineService.getMedicinesByPharmacy(pharmacyId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicineDto>> searchMedicines(@RequestParam String name) {
        return ResponseEntity.ok(medicineService.searchMedicinesByName(name));
    }

    @GetMapping("/available")
    public ResponseEntity<List<MedicineDto>> getAvailableMedicines() {
        return ResponseEntity.ok(medicineService.getAvailableMedicines());
    }
}
