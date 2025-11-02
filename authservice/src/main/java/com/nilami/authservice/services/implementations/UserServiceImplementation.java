package com.nilami.authservice.services.implementations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.exceptions.InsufficientBalanceException;

import com.nilami.authservice.exceptions.UserDoesNotExistException;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.repositories.UserRepository;
import com.nilami.authservice.services.UserService;

@Service
public class UserServiceImplementation implements UserService{
    
  private UserRepository userRepository;
  

   UserServiceImplementation(UserRepository userRepository){
            this.userRepository=userRepository;
   }


    public UserDTO getUserDetails(String userId){
        
        Optional<UserModel> user=userRepository.findById(UUID.fromString(userId));
        if(user.isEmpty()){
         System.out.println("ERROR WHILE FETCHING DATA: user does not exist for userId: "+userId);
            throw new UserDoesNotExistException("User already exists.");
        }


        return UserDTO.toUserDTO(user.get());
    }




  @Override
public Boolean subtractBankBalanceFromUser(String userId, BigDecimal price) {
    try {
        return updateUserBalance(userId, price.negate());
    } catch (Exception e) {
        System.err.println("ERROR: Failed to subtract balance for user: " + userId + " - " + e.getMessage());
        throw e;
    }
}

@Override
public Boolean addBankBalanceToUser(String userId, BigDecimal price) {
    try {
        return updateUserBalance(userId, price);
    } catch (Exception e) {
        System.err.println("ERROR: Failed to add balance for user: " + userId + " - " + e.getMessage());
        throw e;
    }
}

private Boolean updateUserBalance(String userId, BigDecimal amountChange) {
    Optional<UserModel> userFromDatabase = userRepository.findById(UUID.fromString(userId));
    
    if (userFromDatabase.isEmpty()) {
        System.err.println("ERROR WHILE FETCHING DATA: user does not exist for userId: " + userId);
        throw new UserDoesNotExistException("User does not exist with ID: " + userId);
    }

    UserModel user = userFromDatabase.get();
    BigDecimal newBalance = user.getBalance().add(amountChange);

   
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new InsufficientBalanceException("User: " + userId + " has insufficient balance");
    }

    user.setBalance(newBalance);
    userRepository.save(user);

    return true;
}
}
