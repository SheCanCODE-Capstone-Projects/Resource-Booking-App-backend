package org.example.resourcebookingappbackend.Service.Authentication;


import jakarta.transaction.Transactional;
import org.example.resourcebookingappbackend.Enum.Purpose;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Otp;

import org.example.resourcebookingappbackend.Repository.OtpRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class OtpService {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final Duration VALID_FOR =
            Duration.ofMinutes(10);

    private static final Duration RESEND_COOLDOWN =
            Duration.ofMinutes(1);

    private final OtpRepository repository;
    private final EmailService email;

    public OtpService(OtpRepository repository, EmailService email) {
        this.repository = repository;
        this.email = email;

    }

    @Transactional
    public void issue(Account account, Purpose purpose) {
        Optional<Otp> previous = repository.findByAccountAndPurpose(account, purpose);

        Instant now = Instant.now();

        if (previous.isPresent() && previous.get().getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now)) {

            return;
        }

        String code = String.format(
                "%06d",
                RANDOM.nextInt(1_000_000)
        );

        Otp otp = previous.orElseGet(Otp::new);

        otp.setAccount(account);
        otp.setPurpose(purpose);
        otp.setCodeHash(hash(account, purpose, code));
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plus(VALID_FOR));
        otp.setAttempts(0);

        repository.saveAndFlush(otp);

        email.sendOtp(
                account.getUserName(),
                code,
                purpose == otp.getPurpose().EMAIL_VERIFICATION
                        ? "email"
                        : "password reset"
        );
    }


    @Transactional
    public boolean verifyAndConsume(Account account, Purpose purpose, String code) {
        Otp otp = repository
                .findByAccountAndPurpose(account, purpose)
                .orElse(null);

        if (otp == null || !otp.getExpiresAt().isAfter(Instant.now()) || otp.getAttempts() >= 5) {
            return false;
        }

        if (code == null || !code.matches("\\d{6}") || !MessageDigest.isEqual(otp.getCodeHash().getBytes(StandardCharsets.US_ASCII), hash(account, purpose, code).getBytes(StandardCharsets.US_ASCII))) {

            otp.setAttempts(otp.getAttempts() + 1);
            repository.save(otp);

            return false;
        }

        repository.delete(otp);
        return true;
    }


    private String hash(Account account, Purpose purpose, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(
                    new SecretKeySpec(
                            "wertyuiopasdfghjklzxcvbnm1234dfggyhudjsnxbgyhbxd".getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    )
            );

            String value = account.getId()
                    + ":" + purpose.name() + ":" + code;

            return HexFormat.of().formatHex(
                    mac.doFinal(
                            value.getBytes(StandardCharsets.UTF_8)
                    )
            );

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

