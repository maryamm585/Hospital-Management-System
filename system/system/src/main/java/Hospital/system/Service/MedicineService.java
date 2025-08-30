package Hospital.system.Service;

import Hospital.system.DTO.MedicineDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.User;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.Mapper.MedicineMapper;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicineDto addMedicine(MedicineDto dto) {
        log.info("Adding new medicine: {}", dto.getName());

        User pharmacy = userRepository.findById(dto.getPharmacyId())
                .orElseThrow(() -> {
                    log.error("Pharmacy not found with id {}", dto.getPharmacyId());
                    return new ResourceNotFoundException("Pharmacy not found with id " + dto.getPharmacyId());
                });
        Medicine medicine = MedicineMapper.toEntity(dto, pharmacy);
        Medicine saved = medicineRepository.save(medicine);

        log.info("Medicine added successfully with id: {}, name: {}", saved.getId(), saved.getName());
        return MedicineMapper.toDto(saved);
    }

    @Transactional
    public MedicineDto updateMedicine(Long id, MedicineDto dto) {
        log.info("Updating medicine with id {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Medicine not found with id {}", id);
                    return new ResourceNotFoundException("Medicine not found with id " + id);
                });
        medicine.setName(dto.getName());
        medicine.setPrice(dto.getPrice());
        medicine.setStock(dto.getStock());

        if (!medicine.getPharmacy().getId().equals(dto.getPharmacyId())) {
            log.info("Changing pharmacy for medicine {} to pharmacyId={}", id, dto.getPharmacyId());

            User newPharmacy = userRepository.findById(dto.getPharmacyId())
                    .orElseThrow(() -> {
                        log.error("Pharmacy not found with id {}", dto.getPharmacyId());
                        return new ResourceNotFoundException("Pharmacy not found with id " + dto.getPharmacyId());
                    });
            medicine.setPharmacy(newPharmacy);
        }

        Medicine updated = medicineRepository.save(medicine);
        log.info("Medicine updated successfully with id {}", updated.getId());
        return MedicineMapper.toDto(updated);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        log.warn("Deleting medicine with id {}", id);

        if (!medicineRepository.existsById(id)) {
            log.error("Medicine not found with id {}", id);
            throw new ResourceNotFoundException("Medicine not found with id " + id);
        }
        medicineRepository.deleteById(id);
        log.info("Medicine deleted successfully with id {}", id);
    }

    @Transactional(readOnly = true)
    public MedicineDto getMedicineById(Long id) {
        log.debug("Fetching medicine by id {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Medicine not found with id {}", id);
                    return new ResourceNotFoundException("Medicine not found with id " + id);
                });
        return MedicineMapper.toDto(medicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> getMedicinesByPharmacy(Long pharmacyId) {
        log.debug("Fetching medicines for pharmacyId={}", pharmacyId);

        List<MedicineDto> medicines = medicineRepository.findByPharmacy_Id(pharmacyId).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());

        log.info("Fetched {} medicines for pharmacy={}", medicines.size(), pharmacyId);

        return medicines;
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> searchMedicinesByName(String name) {
        log.debug("Searching medicines by name containing '{}'", name);

        List<MedicineDto> medicines = medicineRepository.findByNameContainingIgnoreCase(name).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} medicines matching name='{}'", medicines.size(), name);

        return medicines;
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> getAvailableMedicines() {
        log.debug("Fetching all available medicines (stock > 0)");

        List<MedicineDto> medicines = medicineRepository.findByStockGreaterThan(0).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} available medicines with stock > 0", medicines.size());

        return medicines;
    }
}
