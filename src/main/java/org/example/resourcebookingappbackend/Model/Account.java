package org.example.resourcebookingappbackend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.Role;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
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

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @CreationTimestamp
    private Timestamp createdAt;



}
