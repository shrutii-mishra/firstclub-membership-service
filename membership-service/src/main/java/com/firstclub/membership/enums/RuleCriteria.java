package com.firstclub.membership.enums;

/**
 * Defines the types of criteria used to evaluate tier upgrade eligibility.
 * Adding new criteria here + handling it in TierEvaluationService is all that's needed
 * to support new upgrade logic — fully extensible.
 */
public enum RuleCriteria {
    ORDER_COUNT,    // e.g., placed >= 5 orders in the last month
    ORDER_VALUE,    // e.g., total order value >= ₹5000 in the last month
    USER_COHORT     // e.g., user belongs to cohort "PREMIUM_USER"
}
