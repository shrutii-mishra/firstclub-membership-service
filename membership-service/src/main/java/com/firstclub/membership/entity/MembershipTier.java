package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents a membership tier: Silver, Gold, or Platinum.
 * 'level' allows ordering tiers (Silver=1 < Gold=2 < Platinum=3).
 * Benefits are configurable via TierBenefit — no code change needed to add/modify perks.
 */
@Entity
@Table(name = "membership_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "benefits")
@EqualsAndHashCode(exclude = "benefits")
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // "Silver", "Gold", "Platinum"

    // Level enables easy comparison: can upgrade if newTier.level > currentTier.level
    @Column(nullable = false)
    private int level; // 1=Silver, 2=Gold, 3=Platinum

    // Quick-access fields (duplicated from benefits for easy querying/filtering)
    private boolean freeDelivery;
    private boolean exclusiveDeals;
    private boolean earlyAccessToSales;
    private boolean prioritySupport;

    @OneToMany(mappedBy = "tier", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TierBenefit> benefits;
}
