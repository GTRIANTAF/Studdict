package com.studdict.service;

import com.studdict.model.CheckIn;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.model.StudyTable;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.ReservationParticipantRepository;
import com.studdict.repository.ReservationRepository;
import com.studdict.repository.StudentRepository;
import com.studdict.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationParticipantRepository reservationParticipantRepository;
    private final StudentRepository studentRepository;

    public CheckInService(CheckInRepository checkInRepository,
                          TableRepository tableRepository,
                          ReservationRepository reservationRepository,
                          ReservationParticipantRepository reservationParticipantRepository,
                          StudentRepository studentRepository) {
        this.checkInRepository = checkInRepository;
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.reservationParticipantRepository = reservationParticipantRepository;
        this.studentRepository = studentRepository;
    }

    public StudyTable identifyTable(String qrData) {
        if (qrData == null || qrData.isBlank()) {
            return null;
        }

        Optional<StudyTable> table = tableRepository.findByQrCodeString(qrData);
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

        return reservationParticipantRepository.findByReservationId(reservationId);
    }

    public List<ReservationParticipant> updateCheckedInStatus(List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ReservationParticipant> participants =
                reservationParticipantRepository.findByIdIn(participantIds);

        for (ReservationParticipant participant : participants) {
            participant.setCheckedIn(true);
        }

        return reservationParticipantRepository.saveAll(participants);
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
        if (reservation == null || student == null || scannedQrCode == null || scannedQrCode.isBlank()) {
            return null;
        }

        StudyTable scannedTable = identifyTable(scannedQrCode);
        StudyTable reservationTable = reservation.getTable();

        boolean correctQrCode = scannedTable != null
                && reservationTable != null
                && scannedTable.getId() == reservationTable.getId();

        boolean correctTime = isValidCheckInTime(reservation);

        CheckIn checkIn = new CheckIn();
        checkIn.setReservation(reservation);
        checkIn.setStudent(student);
        checkIn.setTable(reservationTable);
        checkIn.setScannedQrCode(scannedQrCode);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setSuccessful(correctQrCode && correctTime);

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