package com.firstclub.membership.entity;

import com.firstclub.membership.enums.PlanDuration;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a subscription plan: Monthly, Quarterly, or Yearly.
 * 'durationDays' drives the expiry calculation when a user subscribes.
 */
@Entity
@Table(name = "membership_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "Monthly Plan", "Quarterly Plan", etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    @Column(nullable = false)
    private BigDecimal price; // e.g., 99.00

    @Column(nullable = false)
    private int durationDays; // 30, 90, 365

    private boolean active; // soft-disable plans without deleting
}
