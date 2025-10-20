package com.nilami.authservice.services.implementations;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilami.authservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.authservice.dto.BalanceReservationResponse;
import com.nilami.authservice.exceptions.InsufficientBalanceException;
import com.nilami.authservice.exceptions.ReservationNotFoundException;
import com.nilami.authservice.exceptions.UserDoesNotExistException;
import com.nilami.authservice.models.BalanceReservation;
import com.nilami.authservice.models.ReservationStatus;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.repositories.BalanceReservationRepository;
import com.nilami.authservice.repositories.UserRepository;
import com.nilami.authservice.services.BalanceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceServiceImplementation implements BalanceService {

    private final UserRepository userRepository;
    private final BalanceReservationRepository reservationRepository;

    @Transactional
    public BalanceReservationResponse reserveBalance(BalanceReservationRequest request) {

        
        Optional<BalanceReservation> existingReservation = reservationRepository
                .findByIdempotentKey(request.getIdempotentKey());

        if (existingReservation.isPresent()) {
            BalanceReservation reservation = existingReservation.get();

            return convertToResponse(reservation);
        }
        //lock this row becase when the user tries to bid two items at once, the user's data should not be stale
        UserModel user = userRepository.findByIdWithLock(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new UserDoesNotExistException("User not found: " + request.getUserId()));

        BigDecimal availableBalance = user.getAvailableBalance();
        if (availableBalance.compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + availableBalance +
                            ", Required: " + request.getAmount());
        }

        BalanceReservation reservation = BalanceReservation.builder()
                .userId(user.getId())
                .amount(request.getAmount())
                .idempotentKey(request.getIdempotentKey())
                .status(ReservationStatus.RESERVED)
                .build();

        reservation = reservationRepository.save(reservation);

        user.setReservedBalance(user.getReservedBalance().add(request.getAmount()));
        userRepository.save(user);

        return convertToResponse(reservation);
    }

    @Transactional
    public void commitBalanceReservation(String reservationId) {

        BalanceReservation reservation = reservationRepository.findById(UUID.fromString(reservationId))
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {

            throw new IllegalStateException(
                    "Reservation is not in RESERVED status: " + reservation.getStatus());
        }

        // Check if reservation has expired
        if (reservation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Reservation has expired");
        }

        // Lock the user row. This is to ensure that, let's say the person 
        // bids two different items at the same time which it should not happen
        UserModel user = userRepository.findByIdWithLock(reservation.getUserId())
                .orElseThrow(() -> new UserDoesNotExistException(
                        "User not found: " + reservation.getUserId()));

        // Deduct the amount from both balance and reserved balance
        user.setBalance(user.getBalance().subtract(reservation.getAmount()));
        user.setReservedBalance(user.getReservedBalance().subtract(reservation.getAmount()));
        userRepository.save(user);

        // Update reservation status
        reservation.setStatus(ReservationStatus.COMMITTED);
        reservationRepository.save(reservation);

    }

    @Transactional
    public void cancelBalanceReservation(UUID reservationId) {

        BalanceReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {

            return;
        }

        UserModel user = userRepository.findByIdWithLock(reservation.getUserId())
                .orElseThrow(() -> new UserDoesNotExistException(
                        "User not found: " + reservation.getUserId()));

  
        user.setReservedBalance(user.getReservedBalance().subtract(reservation.getAmount()));
        userRepository.save(user);


        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

    }

    private BalanceReservationResponse convertToResponse(BalanceReservation reservation) {
        return BalanceReservationResponse.builder()
                .reservationId(reservation.getId().toString())
                .userId(reservation.getUserId().toString())
                .status(reservation.getStatus().toString())
                .build();
    }
}