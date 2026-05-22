package com.studdict.service;

import com.studdict.model.InviteCode;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.repository.InviteCodeRepository;
import com.studdict.repository.ReservationParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final ReservationParticipantRepository reservationParticipantRepository;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository,
                             ReservationParticipantRepository reservationParticipantRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.reservationParticipantRepository = reservationParticipantRepository;
    }

    public InviteCode generateInviteCode(Reservation reservation, Student host) {
        if (reservation == null || host == null) {
            return null;
        }

        String code = generateUniqueCode();

        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setReservation(reservation);
        inviteCode.setHost(host);
        inviteCode.setCreatedAt(LocalDateTime.now());
        inviteCode.setExpiresAt(LocalDateTime.now().plusHours(2));
        inviteCode.setActive(true);

        return inviteCodeRepository.save(inviteCode);
    }

    public InviteCode findCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return inviteCodeRepository.findByCode(code);
    }

    public boolean validateCode(String code) {
        InviteCode inviteCode = findCode(code);
        return validateCode(inviteCode);
    }

    public boolean validateCode(InviteCode inviteCode) {
        if (inviteCode == null) {
            return false;
        }

        return inviteCode.isValid();
    }

    public Reservation findReservation(InviteCode inviteCode) {
        if (inviteCode == null || !inviteCode.isValid()) {
            return null;
        }

        return inviteCode.getReservation();
    }

    public boolean checkAvailability(Reservation reservation) {
        if (reservation == null || reservation.getReservationId() == null) {
            return false;
        }

        List<ReservationParticipant> participants =
                reservationParticipantRepository.findByReservationId(reservation.getReservationId());

        return participants.size() < reservation.getNumberOfPeople();
    }

    public boolean joinReservation(String code, Student guest) {
        InviteCode inviteCode = findCode(code);
        return joinReservation(inviteCode, guest);
    }

    public boolean joinReservation(InviteCode inviteCode, Student guest) {
        if (inviteCode == null || guest == null) {
            return false;
        }

        if (!inviteCode.isValid()) {
            return false;
        }

        Reservation reservation = inviteCode.getReservation();

        if (reservation == null || reservation.getReservationId() == null) {
            return false;
        }

        if (!checkAvailability(reservation)) {
            return false;
        }

        return addParticipant(reservation, guest);
    }

    public boolean addParticipant(Reservation reservation, Student guest) {
        if (reservation == null || guest == null || reservation.getReservationId() == null) {
            return false;
        }

        List<ReservationParticipant> participants =
                reservationParticipantRepository.findByReservationId(reservation.getReservationId());

        boolean alreadyParticipant = participants.stream()
                .anyMatch(participant -> guest.getStudentId().equals(participant.getStudentId()));

        if (alreadyParticipant) {
            return false;
        }

        ReservationParticipant participant = new ReservationParticipant();
        participant.setReservationId(reservation.getReservationId());
        participant.setStudentId(guest.getStudentId());
        participant.setRole("Guest");
        participant.setCheckedIn(false);

        reservationParticipantRepository.save(participant);

        return true;
    }

    private String generateUniqueCode() {
        String code;

        do {
            code = String.format("%06d", new Random().nextInt(1000000));
        } while (inviteCodeRepository.findByCode(code) != null);

        return code;
    }
}