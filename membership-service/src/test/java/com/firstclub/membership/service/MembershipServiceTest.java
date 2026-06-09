package com.firstclub.membership.service;

import com.firstclub.membership.dto.SubscribeRequest;
import com.firstclub.membership.dto.ChangeTierRequest;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.enums.MembershipStatus;
import com.firstclub.membership.enums.PlanDuration;
import com.firstclub.membership.exception.MembershipException;
import com.firstclub.membership.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // rolls back after every test — keeps tests isolated
class MembershipServiceTest {

    @Autowired private MembershipService membershipService;
    @Autowired private UserRepository userRepository;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;

    private User testUser;
    private MembershipPlan monthlyPlan;
    private MembershipTier silverTier;
    private MembershipTier goldTier;

    @BeforeEach
    void setup() {
        // Runs before every test — like beforeEach in Jest
        testUser = userRepository.save(User.builder()
                .name("Test User").email("test@test.com").cohort("VIP").build());

        monthlyPlan = planRepository.findByActiveTrue().get(0);
        silverTier = tierRepository.findByName("Silver").orElseThrow();
        goldTier = tierRepository.findByName("Gold").orElseThrow();
    }

    // ── Test 1: Happy path — user can subscribe ──────────────────────────────
    @Test
    void shouldSubscribeSuccessfully() {
        SubscribeRequest req = new SubscribeRequest();
        req.setUserId(testUser.getId());
        req.setPlanId(monthlyPlan.getId());
        req.setTierId(silverTier.getId());

        UserMembership membership = membershipService.subscribe(req);

        assertEquals(MembershipStatus.ACTIVE, membership.getStatus());
        assertEquals("Silver", membership.getTier().getName());
        assertNotNull(membership.getExpiryDate());
    }

    // ── Test 2: Cannot subscribe twice ───────────────────────────────────────
    @Test
    void shouldThrowWhenAlreadySubscribed() {
        SubscribeRequest req = new SubscribeRequest();
        req.setUserId(testUser.getId());
        req.setPlanId(monthlyPlan.getId());
        req.setTierId(silverTier.getId());

        membershipService.subscribe(req); // first subscription

        // Second subscription should throw
        assertThrows(MembershipException.class, () -> membershipService.subscribe(req));
    }

    // ── Test 3: Can upgrade tier ──────────────────────────────────────────────
    @Test
    void shouldUpgradeTierSuccessfully() {
        // Subscribe to Silver first
        SubscribeRequest req = new SubscribeRequest();
        req.setUserId(testUser.getId());
        req.setPlanId(monthlyPlan.getId());
        req.setTierId(silverTier.getId());
        membershipService.subscribe(req);

        // VIP cohort qualifies for Gold (rule seeded in DataInitializer)
        ChangeTierRequest upgradeReq = new ChangeTierRequest();
        upgradeReq.setTierId(goldTier.getId());

        UserMembership upgraded = membershipService.upgradeTier(testUser.getId(), upgradeReq);
        assertEquals("Gold", upgraded.getTier().getName());
    }

    // ── Test 4: Cannot downgrade using upgrade endpoint ───────────────────────
    @Test
    void shouldThrowWhenUpgradingToLowerTier() {
        // Subscribe to Gold
        SubscribeRequest req = new SubscribeRequest();
        req.setUserId(testUser.getId());
        req.setPlanId(monthlyPlan.getId());
        req.setTierId(goldTier.getId());
        membershipService.subscribe(req);

        // Try to "upgrade" to Silver (which is lower)
        ChangeTierRequest downReq = new ChangeTierRequest();
        downReq.setTierId(silverTier.getId());

        assertThrows(MembershipException.class,
                () -> membershipService.upgradeTier(testUser.getId(), downReq));
    }

    // ── Test 5: Cancel membership ─────────────────────────────────────────────
    @Test
    void shouldCancelMembership() {
        SubscribeRequest req = new SubscribeRequest();
        req.setUserId(testUser.getId());
        req.setPlanId(monthlyPlan.getId());
        req.setTierId(silverTier.getId());
        membershipService.subscribe(req);

        UserMembership cancelled = membershipService.cancelMembership(testUser.getId());
        assertEquals(MembershipStatus.CANCELLED, cancelled.getStatus());
    }
}