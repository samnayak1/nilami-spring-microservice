package com.nilami.authservice.services.implementations;



import org.springframework.stereotype.Service;

import com.nilami.authservice.controllers.requestTypes.SignupRequest;
import com.nilami.authservice.exceptions.UserAlreadyExistsException;
import com.nilami.authservice.models.Roles;
import com.nilami.authservice.models.UserModel;
import com.nilami.authservice.repositories.UserRepository;
import com.nilami.authservice.services.UserSignupService;


@Service
public class UserSignupServiceImplementation implements UserSignupService{
 


  private UserRepository userRepository;


   UserSignupServiceImplementation(UserRepository userRepository){
            this.userRepository=userRepository;
   }
  

public UserModel signupUser(SignupRequest request) {
    try {
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("ERROR WHILE SIGNING UP: Duplicate email for"+request.getEmail());
            throw new UserAlreadyExistsException("User already exists.");
        }

        UserModel user = new UserModel();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setRole(Roles.CUSTOMER);

        UserModel savedUser = userRepository.save(user);
        System.out.println("User saved: "+savedUser.getName()+savedUser.getId());

        return savedUser;

    } catch (UserAlreadyExistsException e) {
        throw e;
    } catch (Exception e) {
      System.out.println("ERROR WHILE SIGNING UP: Exception for:"+request.getEmail()+" error: "+e.getMessage());
        throw new RuntimeException("Error occurred during user signup", e);
    }
}
    

}
