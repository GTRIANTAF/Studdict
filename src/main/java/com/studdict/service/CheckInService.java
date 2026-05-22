package com.studdict.service;

import com.studdict.model.CheckIn;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.model.StudyTable;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.ParticipantRepository;
import com.studdict.repository.ReservationRepository;
import com.studdict.repository.StudentRepository;
import com.studdict.repository.StudyTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final StudyTableRepository studyTableRepository;
    private final ReservationRepository reservationRepository;
    private final ParticipantRepository participantRepository;
    private final StudentRepository studentRepository;

    public CheckInService(CheckInRepository checkInRepository,
                          StudyTableRepository studyTableRepository,
                          ReservationRepository reservationRepository,
                          ParticipantRepository participantRepository,
                          StudentRepository studentRepository) {
        this.checkInRepository = checkInRepository;
        this.studyTableRepository = studyTableRepository;
        this.reservationRepository = reservationRepository;
        this.participantRepository = participantRepository;
        this.studentRepository = studentRepository;
    }

    public StudyTable identifyTable(String qrData) {
        if (qrData == null || qrData.isBlank()) {
            return null;
        }

        Optional<StudyTable> table = studyTableRepository.findByQrCodeString(qrData);
        return table.orElse(null);
    }

    public ReservationValidationResult validateReservation(Long reservationId, String qrData) {
        if (reservationId == null || qrData == null || qrData.isBlank()) {
            return ReservationValidationResult.INVALID_DATA;
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);

        if (reservationOptional.isEmpty()) {
            return ReservationValidationResult.RESERVATION_NOT_FOUND;
        }

        Reservation reservation = reservationOptional.get();
        StudyTable scannedTable = identifyTable(qrData);

        if (scannedTable == null || reservation.getTable() == null) {
            return ReservationValidationResult.WRONG_TABLE;
        }

        if (reservation.getTable().getId() != scannedTable.getId()) {
            return ReservationValidationResult.WRONG_TABLE;
        }

        if (!isValidCheckInTime(reservation)) {
            return ReservationValidationResult.WRONG_TIME;
        }

        return ReservationValidationResult.VALID_CHECK_IN;
    }

    public List<ReservationParticipant> getParticipants(Long reservationId) {
        if (reservationId == null) {
            return new ArrayList<>();
        }

        return participantRepository.findByReservationId(reservationId);
    }

    public List<ReservationParticipant> updateCheckedInStatus(List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ReservationParticipant> participants =
                participantRepository.findByIdIn(participantIds);

        for (ReservationParticipant participant : participants) {
            participant.setCheckedIn(true);
        }

        return participantRepository.saveAll(participants);
    }

    public boolean associateParticipants(List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return false;
        }

        List<ReservationParticipant> participants =
                participantRepository.findByIdIn(participantIds);

        return !participants.isEmpty();
    }

    public boolean activateServices(List<ReservationParticipant> participants) {
        return participants != null && !participants.isEmpty();
    }

    public List<CheckIn> checkInParticipants(Long reservationId, String qrData, List<Long> participantIds) {
        ReservationValidationResult validationResult = validateReservation(reservationId, qrData);

        if (validationResult != ReservationValidationResult.VALID_CHECK_IN) {
            return new ArrayList<>();
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        StudyTable table = identifyTable(qrData);

        if (reservationOptional.isEmpty() || table == null) {
            return new ArrayList<>();
        }

        Reservation reservation = reservationOptional.get();

        List<ReservationParticipant> checkedInParticipants =
                updateCheckedInStatus(participantIds);

        associateParticipants(participantIds);

        List<CheckIn> checkIns = new ArrayList<>();

        for (ReservationParticipant participant : checkedInParticipants) {
            Optional<Student> studentOptional =
                    studentRepository.findById(participant.getStudentId());

            if (studentOptional.isPresent()) {
                CheckIn checkIn = new CheckIn();
                checkIn.setReservation(reservation);
                checkIn.setStudent(studentOptional.get());
                checkIn.setTable(table);
                checkIn.setScannedQrCode(qrData);
                checkIn.setCheckInTime(LocalDateTime.now());
                checkIn.setSuccessful(true);

                checkIns.add(checkInRepository.save(checkIn));
            }
        }

        activateServices(checkedInParticipants);

        return checkIns;
    }

        public CheckIn checkInStudent(Reservation reservation, Student student, String scannedQrCode) {
        if (reservation == null
                || reservation.getReservationId() == null
                || student == null
                || scannedQrCode == null
                || scannedQrCode.isBlank()) {
            return null;
        }

        Optional<Reservation> reservationOptional =
                reservationRepository.findById(reservation.getReservationId());

        if (reservationOptional.isEmpty()) {
            return null;
        }

        Reservation managedReservation = reservationOptional.get();
        StudyTable scannedTable = identifyTable(scannedQrCode);
        StudyTable reservationTable = managedReservation.getTable();

        boolean correctTable = scannedTable != null
                && reservationTable != null
                && scannedTable.getId() == reservationTable.getId();

        CheckIn checkIn = new CheckIn();
        checkIn.setReservation(managedReservation);
        checkIn.setStudent(student);
        checkIn.setTable(scannedTable);
        checkIn.setScannedQrCode(scannedQrCode);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setSuccessful(correctTable);

        return checkInRepository.save(checkIn);
    }

    private boolean isValidCheckInTime(Reservation reservation) {
        if (reservation == null
                || reservation.getReservationDate() == null
                || reservation.getStartTime() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalTime startTime = reservation.getStartTime();
        LocalTime endTime = startTime.plusMinutes(reservation.getDurationMinutes());

        return reservation.getReservationDate().equals(today)
                && !now.isBefore(startTime)
                && !now.isAfter(endTime);
    }

    public enum ReservationValidationResult {
        VALID_CHECK_IN,
        WRONG_TABLE,
        WRONG_TIME,
        RESERVATION_NOT_FOUND,
        INVALID_DATA
    }
}