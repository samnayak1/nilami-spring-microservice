package com.nilami.authservice.services;

import java.math.BigDecimal;


import com.nilami.authservice.dto.UserDTO;

public interface UserService {
    public UserDTO getUserDetails(String userId);


    public Boolean subtractBankBalanceFromUser(String userId, BigDecimal price);

    public Boolean addBankBalanceToUser(String userId, BigDecimal price);
}
