package com.project.uber.uberApplication.services;

import com.project.uber.uberApplication.dto.SignupDTO;
import com.project.uber.uberApplication.dto.UserDTO;
import com.project.uber.uberApplication.entities.User;
import com.project.uber.uberApplication.entities.enums.Role;
import com.project.uber.uberApplication.repositories.UserRepository;
import com.project.uber.uberApplication.security.JWTService;
import com.project.uber.uberApplication.services.impl.AuthServiceImpl;
import com.project.uber.uberApplication.services.impl.DriverServiceImpl;
import com.project.uber.uberApplication.services.impl.RiderServiceImpl;
import com.project.uber.uberApplication.services.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiderServiceImpl riderService;

    @Mock
    private WalletServiceImpl walletService;

    @Mock
    private DriverServiceImpl driverService;

    @Spy
    private ModelMapper modelMapper;

    @Spy
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private SignupDTO signupDTO;

    @BeforeEach
    void setup(){
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.RIDER));

        signupDTO = new SignupDTO();
        signupDTO.setEmail("test@example.com");
        signupDTO.setPassword("password");
    }

    @Test
    void testLogin_whenSuccess(){
        //arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        //act
        String[] tokens = authService.login(user.getEmail(), user.getPassword());

        //assert
        assertThat(tokens).hasSize(2);
        assertThat(tokens[0]).isEqualTo("accessToken");
        assertThat(tokens[1]).isEqualTo("refreshToken");
    }

    @Test
    void testSignup_whenSuccess(){
        //Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        //Act
        UserDTO userDTO = authService.signup(signupDTO);

        //Assert
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getEmail()).isEqualTo(signupDTO.getEmail());

        verify(riderService).createNewRider(any(User.class));
        verify(walletService).createNewWallet(any(User.class));
    }
}
