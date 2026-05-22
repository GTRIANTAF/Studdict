package com.studdict.service;

import com.studdict.model.InviteCode;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.repository.InviteCodeRepository;
import com.studdict.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final ParticipantRepository participantRepository; // <-- ΠΡΟΣΤΕΘΗΚΕ

    public InviteCodeService(InviteCodeRepository inviteCodeRepository, ParticipantRepository participantRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.participantRepository = participantRepository;
    }

    public InviteCode generateInviteCode(Reservation reservation, Student host) {
        if (reservation == null || host == null) {
            return null;
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(code);
        inviteCode.setReservation(reservation);
        inviteCode.setHost(host);
        inviteCode.setCreatedAt(LocalDateTime.now());
        inviteCode.setExpiresAt(LocalDateTime.now().plusHours(2));
        inviteCode.setActive(true);

        return inviteCodeRepository.save(inviteCode);
    }

    public boolean validateCode(InviteCode inviteCode) {
        return inviteCode != null && inviteCode.isValid();
    }

    public boolean joinReservation(InviteCode inviteCode, Student guest) {
        if (inviteCode == null || guest == null || !inviteCode.isValid()) {
            return false;
        }

        Reservation reservation = inviteCode.getReservation();
        if (reservation == null) {
            return false;
        }

        // --- 🚀 Η ΔΙΚΗ ΣΟΥ ΛΟΓΙΚΗ ΕΝΩΝΕΤΑΙ ΕΔΩ ---
        // Εφόσον ο κωδικός είναι έγκυρος, γράφουμε τον φοιτητή στη βάση!
        ReservationParticipant guestParticipant = new ReservationParticipant();
        guestParticipant.setReservationId(reservation.getReservationId());
        guestParticipant.setStudentId(guest.getStudentId());
        guestParticipant.setRole("Guest");
        participantRepository.save(guestParticipant);

        return true;
    }
}