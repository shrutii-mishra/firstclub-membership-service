package com.firstclub.membership.service;

import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.TierUpgradeRule;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.repository.OrderRepository;
import com.firstclub.membership.repository.TierUpgradeRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evaluates whether a user meets the eligibility criteria for a given membership tier.
 *
 * Design: separated from MembershipService intentionally (Single Responsibility Principle).
 * Adding new criteria types only requires changes here + a new RuleCriteria enum value.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierEvaluationService {

    private final TierUpgradeRuleRepository ruleRepository;
    private final OrderRepository orderRepository;

    /**
     * Returns true if the user satisfies at least one active rule for the given tier.
     * If no rules are configured for a tier, it's open to all (Silver = no rules needed).
     */
    public boolean isEligibleForTier(User user, MembershipTier tier) {
        List<TierUpgradeRule> rules = ruleRepository.findByTierIdAndActiveTrue(tier.getId());

        if (rules.isEmpty()) {
            log.info("No rules configured for tier '{}' — open to all users", tier.getName());
            return true;
        }

        // OR logic: user needs to satisfy any one rule to qualify
        boolean eligible = rules.stream().anyMatch(rule -> evaluateRule(user, rule));
        log.info("User {} eligibility for tier '{}': {}", user.getId(), tier.getName(), eligible);
        return eligible;
    }

    private boolean evaluateRule(User user, TierUpgradeRule rule) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return switch (rule.getCriteria()) {
            case ORDER_COUNT -> {
                Long count = orderRepository.countByUserIdSince(user.getId(), oneMonthAgo);
                log.debug("User {} order count in last 30 days: {} (required: {})",
                        user.getId(), count, rule.getMinOrderCount());
                yield count >= rule.getMinOrderCount();
            }
            case ORDER_VALUE -> {
                BigDecimal total = orderRepository.sumOrderValueByUserIdSince(user.getId(), oneMonthAgo);
                log.debug("User {} order value in last 30 days: {} (required: {})",
                        user.getId(), total, rule.getMinOrderValue());
                yield total.compareTo(rule.getMinOrderValue()) >= 0;
            }
            case USER_COHORT -> {
                boolean match = rule.getRequiredCohort().equalsIgnoreCase(user.getCohort());
                log.debug("User {} cohort: '{}' (required: '{}')",
                        user.getId(), user.getCohort(), rule.getRequiredCohort());
                yield match;
            }
        };
    }
}
