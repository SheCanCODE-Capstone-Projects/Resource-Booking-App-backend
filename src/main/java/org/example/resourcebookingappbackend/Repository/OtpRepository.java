package org.example.resourcebookingappbackend.Repository;

import jakarta.persistence.LockModeType;

import org.example.resourcebookingappbackend.Enum.Purpose;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository
        extends JpaRepository<Otp, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Otp> findByAccountAndPurpose(
            Account account,
            Purpose purpose
    );
}

