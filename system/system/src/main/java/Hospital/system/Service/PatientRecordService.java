package Hospital.system.Service;

import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Entity.PatientRecord;
import Hospital.system.Entity.User;
import Hospital.system.Mapper.PatientRecordMapper;
import Hospital.system.Repository.PatientRecordRepository;
import Hospital.system.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PatientRecordService {

    private final PatientRecordRepository patientRecordRepository;
    private final UserRepository userRepository;

    public PatientRecordService(PatientRecordRepository patientRecordRepository, UserRepository userRepository) {
        this.patientRecordRepository = patientRecordRepository;
        this.userRepository = userRepository;
    }

    public PatientRecordDto createRecord(PatientRecordDto dto) {
        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> {
                    log.error("Patient not found with id {}", dto.getPatientId());
                    return new RuntimeException("Patient not found");
                });

        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> {
                    log.error("Doctor not found with id {}", dto.getDoctorId());
                    return new RuntimeException("Doctor not found");
                });
        PatientRecord record = PatientRecordMapper.toEntity(dto, patient, doctor);
        PatientRecord saved = patientRecordRepository.save(record);

        log.info("Patient record created successfully: recordId={}, patientId={}, doctorId={}",
                saved.getId(), patient.getId(), doctor.getId());

        return PatientRecordMapper.toDto(saved);
    }

    public List<PatientRecordDto> getRecordsByPatient(Long patientId) {
        log.debug("Fetching patient records for patientId={}", patientId);

        List<PatientRecordDto> records = patientRecordRepository.findByPatient_Id(patientId).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
        log.info("Fetched {} records for patientId={}", records.size(), patientId);
        return records;
    }

    public List<PatientRecordDto> getRecordsByDoctor(Long doctorId) {
        log.debug("Fetching patient records for doctorId={}", doctorId);

        List<PatientRecordDto> records = patientRecordRepository.findByDoctor_Id(doctorId).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
        log.info("Fetched {} records for doctorId={}", records.size(), doctorId);
        return records;
    }

    public List<PatientRecordDto> searchRecordsByNotes(String keyword) {
        log.debug("Searching patient records by notes containing '{}'", keyword);

        List<PatientRecordDto> records =  patientRecordRepository.searchByNotes(keyword).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} records matching keyword '{}'", records.size(), keyword);
        return records;
    }
}
