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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionDto prescriptionDto){
        log.debug("Creating prescription: {}", prescriptionDto);

        //doctor
        User doctor = userRepository.findByIdAndRole(prescriptionDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(() -> {
                    log.error("Doctor not found with id {}", prescriptionDto.getDoctorId());
                    return new ResourceNotFoundException("Doctor not found with id " + prescriptionDto.getDoctorId());
                });
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!doctor.getEmail().equals(email)){
            log.error("Doctor not found with id {}", prescriptionDto.getDoctorId());
            throw new AccessDeniedException("You can't prescribe using another doctor's Id");
        }

        //patient
        User patient = userRepository.findByIdAndRole(prescriptionDto.getPatientId(), Role.PATIENT)
                .orElseThrow(() -> {
                    log.error("Patient not found with id {}", prescriptionDto.getPatientId());
                    return new ResourceNotFoundException("Patient not found with id " + prescriptionDto.getPatientId());
                });
        //medicine
        Medicine medicine = medicineRepository.findByName(prescriptionDto.getMedicineName())
                .orElseThrow(() -> {
                    log.error("Medicine not found with name {}", prescriptionDto.getMedicineName());
                    return new ResourceNotFoundException("Medicine not found with name " + prescriptionDto.getMedicineName());
                });

        Prescription prescription = PrescriptionMapper.toEntity(prescriptionDto,doctor,patient,medicine);
        Prescription saved = prescriptionRepository.save(prescription);

        log.info("Prescription created successfully: prescriptionId={}, doctorId={}, patientId={}, medicine={}",
                saved.getId(), doctor.getId(), patient.getId(), medicine.getName());

        return PrescriptionMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching prescriptions for patient email={}", email);

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in patient not found: {}", email);
                    return new ResourceNotFoundException("Logged in patient not found");
                });
        List<Prescription> prescriptions = prescriptionRepository.findByPatient_Id(patient.getId());
        log.info("Fetched {} prescriptions for patientId={}", prescriptions.size(), patient.getId());

        return prescriptions.stream()
                .map(PrescriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getDoctorPrescriptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching prescriptions for doctor email={}", email);

        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in doctor not found: {}", email);
                    return new ResourceNotFoundException("Logged in doctor not found");
                });

        List<Prescription> prescriptions = prescriptionRepository.findByDoctor_Id(doctor.getId());
        log.info("Fetched {} prescriptions for doctorId={}", prescriptions.size(), doctor.getId());

        return prescriptions.stream()
                .map(PrescriptionMapper::toDto)
                .toList();
    }

    @Transactional
    public PrescriptionDto updatePrescription(Long prescriptionId, PrescriptionDto prescriptionDto){
        log.debug("Updating prescription: prescriptionId={}, {}", prescriptionId, prescriptionDto);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescription not found with id {}", prescriptionId);
                    return new ResourceNotFoundException("Prescription not found with id " + prescriptionId);
                });

        //doctor
        User doctor = userRepository.findByIdAndRole(prescriptionDto.getDoctorId(), Role.DOCTOR)
                .orElseThrow(() -> {
                    log.error("Doctor not found with id {}", prescriptionDto.getDoctorId());
                    return new ResourceNotFoundException("Doctor not found with id " + prescriptionDto.getDoctorId());
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!doctor.getEmail().equals(email)){
            log.error("Access denied: logged in doctor {} tried to update prescription {}", email, prescriptionId);
            throw new AccessDeniedException("You can't update the doctor's id");
        }
        // patient cannot be changed (medical complexes)
        if (prescriptionDto.getPatientId() != null && !prescriptionDto.getPatientId().equals(prescription.getPatient().getId())) {
            log.error("Attempt to change patient id for prescription {}", prescriptionId);
            throw new BadRequestException("Patient ID cannot be changed");
        }
        // medicine must exist if updating
        if (prescriptionDto.getMedicineName() != null && !prescriptionDto.getMedicineName().isBlank()) {
            Medicine medicine = medicineRepository.findByName(prescriptionDto.getMedicineName())
                    .orElseThrow(() -> {
                        log.error("Medicine not found: {}", prescriptionDto.getMedicineName());
                        return new ResourceNotFoundException("Medicine not found: " + prescriptionDto.getMedicineName());
                    });
            prescription.setMedicine(medicine);
        }
        prescription.setDosage(prescriptionDto.getDosage());
        prescription.setInstructions(prescriptionDto.getInstructions());

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription updated successfully: prescriptionId={}", saved.getId());

        return PrescriptionMapper.toDto(saved);
    }

    @Transactional
    public void deletePrescription(Long prescriptionId){
        log.debug("Deleting prescription: prescriptionId={}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> {
                    log.error("Prescription not found with id {}", prescriptionId);
                    return new ResourceNotFoundException("Prescription not found with id " + prescriptionId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });

        // Only the doctor who created the prescription can delete it
        if (!prescription.getDoctor().getId().equals(loggedInUser.getId())) {
            log.error("Access denied: user {} tried to delete prescription {}", email, prescriptionId);
            throw new AccessDeniedException("Only the doctor who created the prescription can delete it");
        }

        prescriptionRepository.delete(prescription);
        log.info("Prescription deleted successfully: prescriptionId={}", prescriptionId);
    }
}
