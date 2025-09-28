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
  

   public String signupUser(SignupRequest request){

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists.");
        }

        UserModel user = new UserModel();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setRole(Roles.CUSTOMER);

        UserModel savedUser=userRepository.save(user);
        return savedUser.getId().toString();

   }
    

}
