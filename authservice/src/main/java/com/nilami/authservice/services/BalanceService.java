package com.nilami.authservice.services;

import java.util.UUID;

import com.nilami.authservice.controllers.requestTypes.BalanceReservationRequest;
import com.nilami.authservice.dto.BalanceReservationResponse;

public interface BalanceService {
     public BalanceReservationResponse reserveBalance(BalanceReservationRequest request);
     public void commitBalanceReservation(String reservationId);
     public void cancelBalanceReservation(UUID reservationId);

}
