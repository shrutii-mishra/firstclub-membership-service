package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single configurable benefit attached to a tier.
 * Storing benefits as key-value pairs in DB makes them configurable without code changes.
 *
 * Examples:
 *   benefitKey = "FREE_DELIVERY",    benefitValue = "true"
 *   benefitKey = "DISCOUNT_PERCENT", benefitValue = "10"
 *   benefitKey = "EXCLUSIVE_DEALS",  benefitValue = "true"
 */
@Entity
@Table(name = "tier_benefits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "tier")
@EqualsAndHashCode(exclude = "tier")
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Column(nullable = false)
    private String benefitKey;   // Identifier for the benefit type

    @Column(nullable = false)
    private String benefitValue; // Configurable value

    private String description;  // Human-readable: "10% off on all categories"
}
