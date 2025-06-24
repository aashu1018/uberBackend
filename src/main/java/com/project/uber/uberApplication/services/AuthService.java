package com.project.uber.uberApplication.services;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.SignupDTO;
import com.project.uber.uberApplication.dto.UserDTO;

public interface AuthService {

    UserDTO signup(SignupDTO signupDTO);

    String[] login(String email, String password);

    DriverDTO onboardNewDriver(Long userId, String vehicleId);

    String refreshToken(String refreshToken);
}
