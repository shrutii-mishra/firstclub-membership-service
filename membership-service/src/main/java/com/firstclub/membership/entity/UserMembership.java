package com.firstclub.membership.entity;

import com.firstclub.membership.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks a user's active membership subscription.
 *
 * CONCURRENCY: @Version enables optimistic locking.
 * If two requests try to modify the same membership simultaneously,
 * the second one will get an OptimisticLockException — preventing data corruption.
 * This is important for concurrent upgrade/cancel operations from multiple devices.
 */
@Entity
@Table(name = "user_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Optimistic locking version field — Spring/Hibernate manages this automatically
    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
