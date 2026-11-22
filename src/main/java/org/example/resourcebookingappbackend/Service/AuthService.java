package org.example.resourcebookingappbackend.Service;

import org.example.resourcebookingappbackend.DTO.LoginRequest;
import org.example.resourcebookingappbackend.DTO.LoginResponse;
import org.example.resourcebookingappbackend.DTO.UserRegisterRequest;
import org.example.resourcebookingappbackend.DTO.UserRegisterResponse;
import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Exception.UserAlreadyExist;
import org.example.resourcebookingappbackend.Model.Users;
import org.example.resourcebookingappbackend.Repository.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    public AuthService(UsersRepository usersRepository , PasswordEncoder passwordEncoder , JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public UserRegisterResponse register(UserRegisterRequest userRegisterRequest) {

        if(usersRepository.existsUsersByUserName(userRegisterRequest.getUserName())){
            throw  new UserAlreadyExist("Username already exists");
        }

        Users user = new Users();
        user.setUserName(userRegisterRequest.getUserName());
        user.setFullName(userRegisterRequest.getFirstName() + " " + userRegisterRequest.getLastName());
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));
        user.setRole(Role.USER);

        usersRepository.save(user);

        return UserRegisterResponse.builder()
                .username(user.getUserName())
                .status(HttpStatus.CREATED)
                .message("registered successfully")
                .build();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Users user = usersRepository
                .findUsersByUserName(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));


        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw  new IllegalArgumentException("Incorrect password");
        }

        return LoginResponse.builder()
                .token(jwtService.generateToken(user.getUserName(), user.getRole().name()))
                .userId(user.getId())
                .build();
    }

//    public String editProfile(UserRegisterRequest userRegisterRequest) {
//
//    }
}
