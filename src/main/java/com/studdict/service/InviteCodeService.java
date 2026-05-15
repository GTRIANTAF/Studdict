package com.studdict.service;

import com.studdict.model.InviteCode;
import com.studdict.model.Reservation;
import com.studdict.model.Student;
import com.studdict.repository.InviteCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
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
        if (inviteCode == null) {
            return false;
        }

        return inviteCode.isValid();
    }

    public boolean joinReservation(InviteCode inviteCode, Student guest) {
        if (inviteCode == null || guest == null) {
            return false;
        }

        if (!inviteCode.isValid()) {
            return false;
        }

        Reservation reservation = inviteCode.getReservation();

        if (reservation == null) {
            return false;
        }

        return true;
    }
}