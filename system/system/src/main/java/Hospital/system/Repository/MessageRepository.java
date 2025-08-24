package Hospital.system.Repository;

import Hospital.system.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySender_Id(Long senderId);
    List<Message> findByReceiver_Id(Long receiverId);
    List<Message> findBySender_IdAndReceiver_IdOrderByTimeAsc(Long senderId, Long receiverId);
}
