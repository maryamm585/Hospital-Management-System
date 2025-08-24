package Hospital.system.Repository;

import Hospital.system.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPatient_Id(Long patientId);
    List<Order> findByStatus(Hospital.system.Entity.OrderStatus status);
}