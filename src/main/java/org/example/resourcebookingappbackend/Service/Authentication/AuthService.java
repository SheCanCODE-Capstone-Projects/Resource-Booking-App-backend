package org.example.resourcebookingappbackend.Service.Authentication;

import org.example.resourcebookingappbackend.DTO.Authentication.*;
import org.example.resourcebookingappbackend.Enum.Purpose;
import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Exception.UserAlreadyExist;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private OtpService otpService;

    public AuthService(AccountRepository accountRepository , PasswordEncoder passwordEncoder , JwtService jwtService , OtpService otpService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }


    @Transactional
    public UserRegisterResponse register(UserRegisterRequest userRegisterRequest) {

        if(accountRepository.existsUsersByUserName(userRegisterRequest.getUserName())){
            throw  new UserAlreadyExist("Username already exists");
        }

        Account account = new Account();
        account.setUserName(userRegisterRequest.getUserName());
        account.setFullName(userRegisterRequest.getFirstName() + " " + userRegisterRequest.getLastName());
        account.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));
        account.setRole(Role.USER);
        account.setEmailVerified(false);

        accountRepository.saveAndFlush(account);

        otpService.issue(
                account,
                Purpose.EMAIL_VERIFICATION
        );

        return UserRegisterResponse.builder()
                .username(account.getUserName())
                .status(HttpStatus.CREATED)
                .message( "Registered. Check your email for the verification OTP before logging in.")
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Account account = accountRepository
                .findAccountByUserName(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));



        if ("GOOGLE".equalsIgnoreCase(account.getAuthProvider())) {
            throw new BadCredentialsException("Use Google sign-in");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!account.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify your email before logging in");
        }

        return LoginResponse.builder()
                .token(jwtService.generateToken(account.getUserName(), account.getRole().name()))
                .userId(account.getId())
                .build();
    }


    // Verify registration OTP
    @Transactional
    public boolean verifyEmailResult(VerifyEmailRequest request) {

        Account account = accountRepository.findAccountByUserName(request.getEmail().trim().toLowerCase()).orElse(null);

        if (account == null || account.isEmailVerified()) {
            return false;
        }

        boolean verified = otpService.verifyAndConsume(account, Purpose.EMAIL_VERIFICATION, request.getOtp());

        if (verified) {
            account.setEmailVerified(true);
            accountRepository.save(account);
        }

        return verified;
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        accountRepository.findAccountByUserName(request.getEmail().trim().toLowerCase())
                .filter(user -> !user.isEmailVerified())
                .filter(user -> !"GOOGLE".equalsIgnoreCase(user.getAuthProvider()))
                .ifPresent(user -> otpService.issue(user, Purpose.EMAIL_VERIFICATION)
                );
    }

    // Send forgot-password OTP
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        accountRepository.findAccountByUserName(request.getEmail().trim().toLowerCase())
                .filter(user -> !"GOOGLE".equalsIgnoreCase(user.getAuthProvider()))
                .ifPresent(account -> otpService.issue(account, Purpose.PASSWORD_RESET));
    }

    // Reset forgotten password using OTP
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {

        Account account = accountRepository.findAccountByUserName(request.getEmail().trim().toLowerCase()).orElse(null);

        if (account == null || "GOOGLE".equalsIgnoreCase(account.getAuthProvider())) {
            return false;
        }

        boolean valid = otpService.verifyAndConsume(account, Purpose.PASSWORD_RESET, request.getToken()
        );

        if (!valid) {
            return false;
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword())
        );

        accountRepository.save(account);

        return true;
    }

    // Change password using current password
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication required");
        }

        Account account = accountRepository.findAccountByUserName(auth.getName()).orElseThrow(() ->
                new AccessDeniedException("Authenticated user not found")
        );

        if ("GOOGLE".equalsIgnoreCase(account.getAuthProvider())) {
            throw new BadCredentialsException("Password change is unavailable for Google-only accounts");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new BadCredentialsException("Incorrect current password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword())
        );

        accountRepository.save(account);
    }
}
