package com.studdict.service;

import com.studdict.model.InviteCode;
import com.studdict.model.Reservation;
import com.studdict.model.ReservationParticipant;
import com.studdict.model.Student;
import com.studdict.repository.InviteCodeRepository;
import com.studdict.repository.ParticipantRepository;
import com.studdict.repository.ReservationRepository;
import com.studdict.repository.StudentRepository;
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
    private final StudentRepository studentRepository;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository,
                             ParticipantRepository participantRepository,
                             ReservationRepository reservationRepository,
                             StudentRepository studentRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.participantRepository = participantRepository;
        this.reservationRepository = reservationRepository;
        this.studentRepository = studentRepository;
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

    public InviteCode generateInviteCode(Long reservationId, String hostId) {
        if (reservationId == null || hostId == null || hostId.isBlank()) {
            return null;
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        Optional<Student> hostOptional = studentRepository.findById(hostId);

        if (reservationOptional.isEmpty() || hostOptional.isEmpty()) {
            return null;
        }

        return generateInviteCode(reservationOptional.get(), hostOptional.get());
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

        long participantsCount =
                participantRepository.countByReservationId(reservation.getReservationId());

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

    public boolean joinReservation(String code, String guestId) {
        return joinReservationWithResult(code, guestId) == JoinReservationResult.SUCCESS;
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

    public JoinReservationResult joinReservationWithResult(String code, String guestId) {
        if (code == null || code.isBlank()) {
            return JoinReservationResult.INVALID_OR_EXPIRED_CODE;
        }

        if (guestId == null || guestId.isBlank()) {
            return JoinReservationResult.STUDENT_NOT_FOUND;
        }

        Optional<Student> guestOptional = studentRepository.findById(guestId);

        if (guestOptional.isEmpty()) {
            return JoinReservationResult.STUDENT_NOT_FOUND;
        }

        InviteCode inviteCode = findCode(code);

        if (inviteCode == null || !inviteCode.isValid()) {
            return JoinReservationResult.INVALID_OR_EXPIRED_CODE;
        }

        Reservation reservation = inviteCode.getReservation();

        if (reservation == null || reservation.getReservationId() == null) {
            return JoinReservationResult.RESERVATION_NOT_FOUND;
        }

        if (!checkAvailability(reservation)) {
            return JoinReservationResult.RESERVATION_FULL;
        }

        Student guest = guestOptional.get();

        List<ReservationParticipant> participants =
                participantRepository.findByReservationId(reservation.getReservationId());

        boolean alreadyParticipant = participants.stream()
                .anyMatch(participant -> guest.getStudentId().equals(participant.getStudentId()));

        if (alreadyParticipant) {
            return JoinReservationResult.ALREADY_PARTICIPANT;
        }

        ReservationParticipant participant = new ReservationParticipant();
        participant.setReservationId(reservation.getReservationId());
        participant.setStudentId(guest.getStudentId());
        participant.setRole("Guest");
        participant.setCheckedIn(false);

        participantRepository.save(participant);

        return JoinReservationResult.SUCCESS;
    }

    private String generateUniqueCode() {
        String code;

        do {
            code = String.format("%06d", new Random().nextInt(1000000));
        } while (inviteCodeRepository.findByCode(code) != null);

        return code;
    }

    public enum JoinReservationResult {
        SUCCESS,
        INVALID_OR_EXPIRED_CODE,
        RESERVATION_NOT_FOUND,
        RESERVATION_FULL,
        STUDENT_NOT_FOUND,
        ALREADY_PARTICIPANT
    }
}