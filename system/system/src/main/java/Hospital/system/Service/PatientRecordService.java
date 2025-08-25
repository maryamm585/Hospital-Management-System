package Hospital.system.Service;

import Hospital.system.DTO.PatientRecordDto;
import Hospital.system.Entity.PatientRecord;
import Hospital.system.Entity.User;
import Hospital.system.Mapper.PatientRecordMapper;
import Hospital.system.Repository.PatientRecordRepository;
import Hospital.system.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientRecordService {

    private final PatientRecordRepository patientRecordRepository;
    private final UserRepository userRepository;

    public PatientRecordService(PatientRecordRepository patientRecordRepository, UserRepository userRepository) {
        this.patientRecordRepository = patientRecordRepository;
        this.userRepository = userRepository;
    }

    public PatientRecordDto createRecord(PatientRecordDto dto) {
        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        PatientRecord record = PatientRecordMapper.toEntity(dto, patient, doctor);
        PatientRecord saved = patientRecordRepository.save(record);

        return PatientRecordMapper.toDto(saved);
    }

    public List<PatientRecordDto> getRecordsByPatient(Long patientId) {
        return patientRecordRepository.findByPatient_Id(patientId).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PatientRecordDto> getRecordsByDoctor(Long doctorId) {
        return patientRecordRepository.findByDoctor_Id(doctorId).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PatientRecordDto> searchRecordsByNotes(String keyword) {
        return patientRecordRepository.searchByNotes(keyword).stream()
                .map(PatientRecordMapper::toDto)
                .collect(Collectors.toList());
    }
}
