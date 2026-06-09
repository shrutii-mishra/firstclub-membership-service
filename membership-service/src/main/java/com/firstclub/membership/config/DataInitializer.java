package com.firstclub.membership.config;

import com.firstclub.membership.entity.*;
import com.firstclub.membership.enums.PlanDuration;
import com.firstclub.membership.enums.RuleCriteria;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with realistic demo data when the application starts.
 * This makes the app immediately demo-able without manual setup.
 *
 * Data seeded:
 *   - 3 membership plans (Monthly/Quarterly/Yearly)
 *   - 3 membership tiers (Silver/Gold/Platinum) with benefits
 *   - Tier eligibility rules (configurable)
 *   - 2 demo users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final TierUpgradeRuleRepository ruleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedPlans();
        seedTiersWithBenefits();
        seedTierRules();
        seedDemoUsers();
        log.info("✅ Demo data seeded successfully. App is ready!");
        log.info("📖 H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:membershipdb)");
    }

    private void seedPlans() {
        List<MembershipPlan> plans = List.of(
            MembershipPlan.builder()
                .name("Monthly Plan")
                .duration(PlanDuration.MONTHLY)
                .price(new BigDecimal("99.00"))
                .durationDays(30)
                .active(true)
                .build(),
            MembershipPlan.builder()
                .name("Quarterly Plan")
                .duration(PlanDuration.QUARTERLY)
                .price(new BigDecimal("249.00"))
                .durationDays(90)
                .active(true)
                .build(),
            MembershipPlan.builder()
                .name("Yearly Plan")
                .duration(PlanDuration.YEARLY)
                .price(new BigDecimal("799.00"))
                .durationDays(365)
                .active(true)
                .build()
        );
        planRepository.saveAll(plans);
        log.info("Seeded {} membership plans", plans.size());
    }

    private void seedTiersWithBenefits() {
        // ── Silver (Level 1) ──────────────────────────────────────────────────
        MembershipTier silver = MembershipTier.builder()
                .name("Silver")
                .level(1)
                .freeDelivery(false)
                .exclusiveDeals(false)
                .earlyAccessToSales(false)
                .prioritySupport(false)
                .build();
        silver = tierRepository.save(silver);

        List<TierBenefit> silverBenefits = List.of(
            benefit(silver, "DISCOUNT_PERCENT", "5", "5% discount on all categories"),
            benefit(silver, "WELCOME_COUPON",   "true", "Welcome coupon on first order")
        );
        silver.setBenefits(silverBenefits);
        tierRepository.save(silver);

        // ── Gold (Level 2) ────────────────────────────────────────────────────
        MembershipTier gold = MembershipTier.builder()
                .name("Gold")
                .level(2)
                .freeDelivery(true)
                .exclusiveDeals(true)
                .earlyAccessToSales(false)
                .prioritySupport(false)
                .build();
        gold = tierRepository.save(gold);

        List<TierBenefit> goldBenefits = List.of(
            benefit(gold, "DISCOUNT_PERCENT", "10", "10% discount on all categories"),
            benefit(gold, "FREE_DELIVERY",    "true", "Free delivery on all eligible orders"),
            benefit(gold, "EXCLUSIVE_DEALS",  "true", "Access to exclusive member-only deals")
        );
        gold.setBenefits(goldBenefits);
        tierRepository.save(gold);

        // ── Platinum (Level 3) ────────────────────────────────────────────────
        MembershipTier platinum = MembershipTier.builder()
                .name("Platinum")
                .level(3)
                .freeDelivery(true)
                .exclusiveDeals(true)
                .earlyAccessToSales(true)
                .prioritySupport(true)
                .build();
        platinum = tierRepository.save(platinum);

        List<TierBenefit> platinumBenefits = List.of(
            benefit(platinum, "DISCOUNT_PERCENT",     "15",   "15% discount on all categories"),
            benefit(platinum, "FREE_DELIVERY",        "true", "Free delivery on all eligible orders"),
            benefit(platinum, "EXCLUSIVE_DEALS",      "true", "Access to exclusive member-only deals"),
            benefit(platinum, "EARLY_ACCESS_SALES",   "true", "Early access to sales — 24hrs before others"),
            benefit(platinum, "PRIORITY_SUPPORT",     "true", "Priority customer support"),
            benefit(platinum, "EXCLUSIVE_COUPONS",    "3",    "3 exclusive coupons per month")
        );
        platinum.setBenefits(platinumBenefits);
        tierRepository.save(platinum);

        log.info("Seeded Silver, Gold, and Platinum tiers with benefits");
    }

    private void seedTierRules() {
        MembershipTier gold = tierRepository.findByName("Gold").orElseThrow();
        MembershipTier platinum = tierRepository.findByName("Platinum").orElseThrow();

        // Gold eligibility: either 3+ orders OR ₹3000+ spend in last 30 days
        ruleRepository.saveAll(List.of(
            TierUpgradeRule.builder()
                .tier(gold)
                .criteria(RuleCriteria.ORDER_COUNT)
                .minOrderCount(3)
                .active(true)
                .build(),
            TierUpgradeRule.builder()
                .tier(gold)
                .criteria(RuleCriteria.ORDER_VALUE)
                .minOrderValue(new BigDecimal("3000.00"))
                .active(true)
                .build()
        ));

        // Platinum eligibility: either 10+ orders OR ₹10000+ spend OR belongs to "VIP" cohort
        ruleRepository.saveAll(List.of(
            TierUpgradeRule.builder()
                .tier(platinum)
                .criteria(RuleCriteria.ORDER_COUNT)
                .minOrderCount(10)
                .active(true)
                .build(),
            TierUpgradeRule.builder()
                .tier(platinum)
                .criteria(RuleCriteria.ORDER_VALUE)
                .minOrderValue(new BigDecimal("10000.00"))
                .active(true)
                .build(),
            TierUpgradeRule.builder()
                .tier(platinum)
                .criteria(RuleCriteria.USER_COHORT)
                .requiredCohort("VIP")
                .active(true)
                .build()
        ));

        log.info("Seeded tier upgrade rules");
    }

    private void seedDemoUsers() {
        userRepository.saveAll(List.of(
            User.builder().name("Alice Sharma").email("alice@example.com").cohort("REGULAR").build(),
            User.builder().name("Bob Verma").email("bob@example.com").cohort("VIP").build()
        ));
        log.info("Seeded 2 demo users (alice@example.com, bob@example.com)");
    }

    private TierBenefit benefit(MembershipTier tier, String key, String value, String desc) {
        return TierBenefit.builder()
                .tier(tier)
                .benefitKey(key)
                .benefitValue(value)
                .description(desc)
                .build();
    }
}
