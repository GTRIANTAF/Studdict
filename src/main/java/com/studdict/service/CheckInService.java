package com.studdict.service;

import com.studdict.model.CheckIn;
import com.studdict.model.Reservation;
import com.studdict.model.Student;
import com.studdict.model.StudyTable;
import com.studdict.repository.CheckInRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //added annotation (mariosk)

import java.time.LocalDateTime;

@Service
@Transactional //added annotation (mariosk)
public class CheckInService {

    private final CheckInRepository checkInRepository;

    public CheckInService(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    public CheckIn checkInStudent(Reservation reservation, Student student, String scannedQrCode) {
        if (reservation == null || student == null || scannedQrCode == null || scannedQrCode.isEmpty()) {
            return null;
        }

        StudyTable table = reservation.getTable();

        boolean correctQrCode = table != null
                && table.getQrCodeString() != null
                && table.getQrCodeString().equals(scannedQrCode);

        CheckIn checkIn = new CheckIn();
        checkIn.setReservation(reservation);
        checkIn.setStudent(student);
        checkIn.setTable(table);
        checkIn.setScannedQrCode(scannedQrCode);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setSuccessful(correctQrCode);

        return checkInRepository.save(checkIn);
    }
}