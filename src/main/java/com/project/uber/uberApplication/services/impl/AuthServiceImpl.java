package com.project.uber.uberApplication.services.impl;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.SignupDTO;
import com.project.uber.uberApplication.dto.UserDTO;
import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.User;
import com.project.uber.uberApplication.entities.enums.Role;
import com.project.uber.uberApplication.exceptions.ResourceNotFoundException;
import com.project.uber.uberApplication.exceptions.RuntimeConflictException;
import com.project.uber.uberApplication.repositories.UserRepository;
import com.project.uber.uberApplication.security.JWTService;
import com.project.uber.uberApplication.services.AuthService;
import com.project.uber.uberApplication.services.DriverService;
import com.project.uber.uberApplication.services.RiderService;
import com.project.uber.uberApplication.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ModelMapper mapper;
    private final UserRepository userRepository;
    private final RiderService riderService;
    private final WalletService walletService;
    private final DriverService driverService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Override
    @Transactional
    public UserDTO signup(SignupDTO signupDTO) {
        User user = userRepository.findByEmail(signupDTO.getEmail()).orElse(null);
        if(user!=null){
            throw new RuntimeConflictException("Cannot signup, user already exists with email: " +
                    signupDTO.getEmail());
        }

        User mappedUser = mapper.map(signupDTO, User.class);
        mappedUser.setRoles(Set.of(Role.RIDER));
        mappedUser.setPassword(passwordEncoder.encode(mappedUser.getPassword()));
        User savedUser = userRepository.save(mappedUser);

        //create user related activities
        riderService.createNewRider(savedUser);
        walletService.createNewWallet(savedUser);

        return mapper.map(savedUser, UserDTO.class);
    }

    @Override
    public String[] login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new String[]{accessToken, refreshToken};
    }

    @Override
    public DriverDTO onboardNewDriver(Long userId, String vehicleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if(user.getRoles().contains(Role.DRIVER)){
            throw new RuntimeConflictException("User with userId " + userId + " is already onboarded as a driver");
        }

        Driver createDriver = Driver.builder()
                .user(user)
                .rating(0.0)
                .vehicleId(vehicleId)
                .available(true)
                .build();

        user.getRoles().add(Role.DRIVER);
        userRepository.save(user);

        Driver savedDriver = driverService.createNewDriver(createDriver);

        return mapper.map(savedDriver, DriverDTO.class);
    }

    @Override
    public String refreshToken(String refreshToken) {
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return jwtService.generateAccessToken(user);
    }
}
