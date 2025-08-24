package Hospital.system.Repository;

import Hospital.system.Entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByPharmacy_Id(Long pharmacyId);
    List<Medicine> findByNameContainingIgnoreCase(String name);
    List<Medicine> findByStockGreaterThan(Integer minStock);
}
