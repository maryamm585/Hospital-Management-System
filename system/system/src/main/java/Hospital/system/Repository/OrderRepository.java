package Hospital.system.Repository;

import Hospital.system.Entity.Order;
import Hospital.system.Entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPatient_Id(Long patientId);

    List<Order>findByPharmacy_Id(Long pharmacyId);

    List<Order> findByPatient_IdAndStatus(Long patientId, OrderStatus status);

    List<Order> findByPharmacy_IdAndStatus(Long pharmacyId ,OrderStatus status);
}