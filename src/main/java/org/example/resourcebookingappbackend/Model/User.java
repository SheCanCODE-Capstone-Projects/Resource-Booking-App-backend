package org.example.resourcebookingappbackend.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.resourcebookingappbackend.Enum.Role;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        @Column(unique=true , nullable = false)
        private String email;
        @Column(nullable= false)
        private String passwordHash;
        @Enumerated(EnumType.STRING)
        @Column(nullable= false)
        private Role role;
        @Column(nullable= false)
        private LocalDateTime createdAt;
}
