package com.nilami.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilami.authservice.dto.UserDTO;
import com.nilami.authservice.exceptions.InsufficientBalanceException;
import com.nilami.authservice.exceptions.UserDoesNotExistException;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.repositories.UserRepository;
import com.nilami.authservice.services.implementations.UserServiceImplementation;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImplementation userService;

    private String testUserId;
    private UUID testUUID;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testUserId = testUUID.toString();
        testUser = new UserModel();
        testUser.setId(testUUID);
        testUser.setBalance(new BigDecimal("1000.00"));
    }


    @Test
    void getUserDetails_WhenUserExists_ExpectUserDTO() {
      
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));

       
        UserDTO result = userService.getUserDetails(testUserId);

 
        assertNotNull(result);
        verify(userRepository, times(1)).findById(testUUID);
    }

    @Test
    void getUserDetails_WhenUserDoesNotExist_ThrowsException() {
       
        when(userRepository.findById(testUUID)).thenReturn(Optional.empty());

        UserDoesNotExistException exception = assertThrows(
            UserDoesNotExistException.class,
            () -> userService.getUserDetails(testUserId)
        );

        assertEquals("User already exists.", exception.getMessage());
        verify(userRepository, times(1)).findById(testUUID);
    }



    @Test
    void subtractBankBalanceFromUser_WhenSufficientBalance_ExpectTrue() {

        BigDecimal subtractAmount = new BigDecimal("500.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Boolean result = userService.subtractBankBalanceFromUser(testUserId, subtractAmount);

      
        assertTrue(result);
        assertEquals(new BigDecimal("500.00"), testUser.getBalance());
        verify(userRepository, times(1)).findById(testUUID);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void subtractBankBalanceFromUser_WhenInsufficientBalance_ThrowsException() {
      
        BigDecimal subtractAmount = new BigDecimal("1500.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));

        InsufficientBalanceException exception = assertThrows(
            InsufficientBalanceException.class,
            () -> userService.subtractBankBalanceFromUser(testUserId, subtractAmount)
        );

        assertTrue(exception.getMessage().contains("has insufficient balance"));
        verify(userRepository, times(1)).findById(testUUID);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void subtractBankBalanceFromUser_WhenUserDoesNotExist_ThrowsException() {
      
        BigDecimal subtractAmount = new BigDecimal("100.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.empty());

     
        UserDoesNotExistException exception = assertThrows(
            UserDoesNotExistException.class,
            () -> userService.subtractBankBalanceFromUser(testUserId, subtractAmount)
        );

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(userRepository, times(1)).findById(testUUID);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void subtractBankBalanceFromUser_ExactBalance_ExpectTrue() {
    
        BigDecimal subtractAmount = new BigDecimal("1000.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        
        Boolean result = userService.subtractBankBalanceFromUser(testUserId, subtractAmount);

       
        assertTrue(result);
        assertEquals(0, testUser.getBalance().compareTo(BigDecimal.ZERO));
        verify(userRepository, times(1)).save(testUser);
    }

 

    @Test
    void addBankBalanceToUser_WhenUserExists_ExpectTrue() {
      
        BigDecimal addAmount = new BigDecimal("250.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

     
        Boolean result = userService.addBankBalanceToUser(testUserId, addAmount);

   
        assertTrue(result);
        assertEquals(new BigDecimal("1250.00"), testUser.getBalance());
        verify(userRepository, times(1)).findById(testUUID);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void addBankBalanceToUser_WhenUserDoesNotExist_ThrowsException() {
      
        BigDecimal addAmount = new BigDecimal("100.00");
        when(userRepository.findById(testUUID)).thenReturn(Optional.empty());

  
        UserDoesNotExistException exception = assertThrows(
            UserDoesNotExistException.class,
            () -> userService.addBankBalanceToUser(testUserId, addAmount)
        );

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(userRepository, times(1)).findById(testUUID);
        verify(userRepository, never()).save(any(UserModel.class));
    }





    @Test
    void addBankBalanceToUser_WithLargeAmount_WorksCorrectly() {
     
        BigDecimal largeAmount = new BigDecimal("999999.99");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

   
        Boolean result = userService.addBankBalanceToUser(testUserId, largeAmount);

   
        assertTrue(result);
        assertEquals(new BigDecimal("1000999.99"), testUser.getBalance());
    }

    @Test
    void subtractBankBalanceFromUser_ResultingInNegativeBalance_ThrowsException() {
      
        testUser.setBalance(new BigDecimal("100.00"));
        BigDecimal subtractAmount = new BigDecimal("100.01");
        when(userRepository.findById(testUUID)).thenReturn(Optional.of(testUser));

   
        assertThrows(
            InsufficientBalanceException.class,
            () -> userService.subtractBankBalanceFromUser(testUserId, subtractAmount)
        );
    }
}
