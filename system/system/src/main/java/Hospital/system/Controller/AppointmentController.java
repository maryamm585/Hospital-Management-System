package Hospital.system.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class AppointmentController {
}
