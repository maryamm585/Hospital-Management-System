package Hospital.system.Service;

import Hospital.system.DTO.MedicineDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.User;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.Mapper.MedicineMapper;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicineDto addMedicine(MedicineDto dto) {
        User pharmacy = userRepository.findById(dto.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id " + dto.getPharmacyId()));

        Medicine medicine = MedicineMapper.toEntity(dto, pharmacy);
        Medicine saved = medicineRepository.save(medicine);

        return MedicineMapper.toDto(saved);
    }

    @Transactional
    public MedicineDto updateMedicine(Long id, MedicineDto dto) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));

        medicine.setName(dto.getName());
        medicine.setPrice(dto.getPrice());
        medicine.setStock(dto.getStock());

        if (!medicine.getPharmacy().getId().equals(dto.getPharmacyId())) {
            User newPharmacy = userRepository.findById(dto.getPharmacyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id " + dto.getPharmacyId()));
            medicine.setPharmacy(newPharmacy);
        }

        Medicine updated = medicineRepository.save(medicine);
        return MedicineMapper.toDto(updated);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicine not found with id " + id);
        }
        medicineRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MedicineDto getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
        return MedicineMapper.toDto(medicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> getMedicinesByPharmacy(Long pharmacyId) {
        return medicineRepository.findByPharmacy_Id(pharmacyId).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> searchMedicinesByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> getAvailableMedicines() {
        return medicineRepository.findByStockGreaterThan(0).stream()
                .map(MedicineMapper::toDto)
                .collect(Collectors.toList());
    }
}
