package org.example.resourcebookingappbackend.Repository;

import jakarta.validation.constraints.Email;
import org.example.resourcebookingappbackend.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsUsersByUserName(@Email(message = "provide the valid email") String userName);

    Optional<Account> findAccountByUserName(@Email(message = "provide the valid email") String userName);

    List<Account> findAllByRole(String role);

}