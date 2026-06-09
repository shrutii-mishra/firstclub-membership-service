package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user/customer on the platform.
 * 'cohort' lets us group users (e.g., "STUDENT", "PREMIUM_USER") for tier-eligibility rules.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Cohort allows targeting users for tier upgrades (e.g., all "VIP" users get Gold automatically)
    private String cohort;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
