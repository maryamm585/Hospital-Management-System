package Hospital.system.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DoctorAvailabilityDto {
    //only a response dto
    private Long doctorId;
    private String doctorName;
    private List<LocalDateTime> availableTimes;
}
