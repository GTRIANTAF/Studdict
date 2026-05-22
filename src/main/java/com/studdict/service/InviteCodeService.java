package com.studdict.service;

import com.studdict.model.InviteCode;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.repository.InviteCodeRepository;
import com.studdict.repository.ParticipantRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final ParticipantRepository participantRepository;
    private final ReservationRepository reservationRepository;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository,
                             ParticipantRepository participantRepository,
                             ReservationRepository reservationRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.participantRepository = participantRepository;
        this.reservationRepository = reservationRepository;
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
        return inviteCode != null && inviteCode.isValid();
    }

    public Reservation findReservation(InviteCode inviteCode) {
        if (inviteCode == null || !inviteCode.isValid()) {
            return null;
        }

        return inviteCode.getReservation();
    }

    public Reservation findReservation(Long inviteCodeId) {
        if (inviteCodeId == null) {
            return null;
        }

        Optional<InviteCode> inviteCodeOptional = inviteCodeRepository.findById(inviteCodeId);

        if (inviteCodeOptional.isEmpty()) {
            return null;
        }

        return findReservation(inviteCodeOptional.get());
    }

    public boolean checkAvailability(Reservation reservation) {
        if (reservation == null || reservation.getReservationId() == null) {
            return false;
        }

        long participantsCount = participantRepository.countByReservationId(reservation.getReservationId());

        return participantsCount < reservation.getNumberOfPeople();
    }

    public boolean checkAvailability(Long reservationId) {
        if (reservationId == null) {
            return false;
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);

        return reservationOptional.filter(this::checkAvailability).isPresent();
    }

    public boolean joinReservation(String code, Student guest) {
        InviteCode inviteCode = findCode(code);
        return joinReservation(inviteCode, guest);
    }

    public boolean joinReservation(InviteCode inviteCode, Student guest) {
        if (inviteCode == null || guest == null || !inviteCode.isValid()) {
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
                participantRepository.findByReservationId(reservation.getReservationId());

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

        participantRepository.save(participant);

        return true;
    }

    public boolean addParticipant(Long reservationId, Student guest) {
        if (reservationId == null || guest == null) {
            return false;
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);

        return reservationOptional
                .map(reservation -> addParticipant(reservation, guest))
                .orElse(false);
    }

    private String generateUniqueCode() {
        String code;

        do {
            code = String.format("%06d", new Random().nextInt(1000000));
        } while (inviteCodeRepository.findByCode(code) != null);

        return code;
    }
}