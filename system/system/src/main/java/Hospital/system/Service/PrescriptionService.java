package Hospital.system.Service;
import Hospital.system.DTO.PrescriptionDto;
import Hospital.system.Entity.Medicine;
import Hospital.system.Entity.Prescription;
import Hospital.system.Entity.Role;
import Hospital.system.Entity.User;
import Hospital.system.Mapper.PrescriptionMapper;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.PrescriptionRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionDto prescriptionDto){
        //doctor
        User doctor = userRepository.findByIdAndRole(prescriptionDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + prescriptionDto.getDoctorId()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!doctor.getEmail().equals(email)){
            throw new AccessDeniedException("You can't prescribe using another doctor's Id");
        }

        //patient
        User patient = userRepository.findByIdAndRole(prescriptionDto.getPatientId(), Role.PATIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + prescriptionDto.getPatientId()));

        //medicine
        Medicine medicine = medicineRepository.findByName(prescriptionDto.getMedicineName())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with name " + prescriptionDto.getMedicineName()));

        Prescription prescription = PrescriptionMapper.toEntity(prescriptionDto,doctor,patient,medicine);
        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in patient not found"));

        List<Prescription> prescriptions = prescriptionRepository.findByPatient_Id(patient.getId());

        return prescriptions.stream()
                .map(PrescriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getDoctorPrescriptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in doctor not found"));

        List<Prescription> prescriptions = prescriptionRepository.findByDoctor_Id(doctor.getId());

        return prescriptions.stream()
                .map(PrescriptionMapper::toDto)
                .toList();
    }

    @Transactional
    public PrescriptionDto updatePrescription(Long prescriptionId, PrescriptionDto prescriptionDto){
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id " + prescriptionId));
        //doctor
        User doctor = userRepository.findByIdAndRole(prescriptionDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + prescriptionDto.getDoctorId()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!doctor.getEmail().equals(email)){
            throw new AccessDeniedException("You can't update the doctor's id");
        }
        // patient cannot be changed (medical complexes)
        if (prescriptionDto.getPatientId() != null && !prescriptionDto.getPatientId().equals(prescription.getPatient().getId())) {
            throw new BadRequestException("Patient ID cannot be changed");
        }
        // medicine must exist if updating
        if (prescriptionDto.getMedicineName() != null && !prescriptionDto.getMedicineName().isBlank()) {
            Medicine medicine = medicineRepository.findByName(prescriptionDto.getMedicineName())
                    .orElseThrow( () -> new ResourceNotFoundException("Medicine not found: " + prescriptionDto.getMedicineName()));
            prescription.setMedicine(medicine);
        }
        prescription.setDosage(prescriptionDto.getDosage());
        prescription.setInstructions(prescriptionDto.getInstructions());

        Prescription saved = prescriptionRepository.save(prescription);
        return PrescriptionMapper.toDto(saved);
    }

    @Transactional
    public void deletePrescription(Long prescriptionId){
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id " + prescriptionId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        // Only the doctor who created the prescription can delete it
        if (!prescription.getDoctor().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("Only the doctor who created the prescription can delete it");
        }

        prescriptionRepository.delete(prescription);
    }
}
