package org.example.resourcebookingappbackend.Repository;

import jakarta.validation.constraints.Email;
import org.example.resourcebookingappbackend.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    boolean existsUsersByUserName(@Email(message = "provide the valid email") String userName);

    Optional<Users> findUsersByUserName(@Email(message = "provide the valid email") String userName);
}