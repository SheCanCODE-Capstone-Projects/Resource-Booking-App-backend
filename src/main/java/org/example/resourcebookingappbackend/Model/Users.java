package org.example.resourcebookingappbackend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.Role;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Email(message = "provide the valid email")
    @Column(unique = true)
    private String userName;

    private String fullName;

   @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    private String authProvider;

    @CreationTimestamp
    private Timestamp createdAt;



}
