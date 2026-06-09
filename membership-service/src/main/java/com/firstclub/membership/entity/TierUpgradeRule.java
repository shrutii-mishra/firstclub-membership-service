package com.firstclub.membership.entity;

import com.firstclub.membership.enums.RuleCriteria;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Configurable rules that define eligibility for a tier.
 * Each rule belongs to a tier and specifies ONE criteria.
 *
 * Multiple rules for the same tier use OR logic:
 *   "User qualifies for Gold if they have >= 5 orders OR total spend >= ₹5000"
 *
 * Being DB-configurable means product managers can adjust rules without deployments.
 */
@Entity
@Table(name = "tier_upgrade_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierUpgradeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleCriteria criteria;

    // Used when criteria = ORDER_COUNT
    private Integer minOrderCount;

    // Used when criteria = ORDER_VALUE (total in last 30 days)
    private BigDecimal minOrderValue;

    // Used when criteria = USER_COHORT
    private String requiredCohort;

    private boolean active; // Disable a rule without deleting it
}
