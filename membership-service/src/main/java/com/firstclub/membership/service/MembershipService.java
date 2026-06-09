package com.firstclub.membership.service;

import com.firstclub.membership.dto.ChangeTierRequest;
import com.firstclub.membership.dto.CreateOrderRequest;
import com.firstclub.membership.dto.CreateUserRequest;
import com.firstclub.membership.dto.SubscribeRequest;
import com.firstclub.membership.entity.*;

import java.util.List;

/**
 * Core membership service contract.
 * Programming to an interface allows swapping implementations easily
 * and makes unit testing simple (just mock this interface).
 */
public interface MembershipService {

    // ── Plan & Tier Discovery ──────────────────────────────────────
    List<MembershipPlan> getAllActivePlans();
    List<MembershipTier> getAllTiers();

    // ── User Management ────────────────────────────────────────────
    User createUser(CreateUserRequest request);
    User getUser(Long userId);

    // ── Subscription Lifecycle ─────────────────────────────────────
    UserMembership subscribe(SubscribeRequest request);
    UserMembership upgradeTier(Long userId, ChangeTierRequest request);
    UserMembership downgradeTier(Long userId, ChangeTierRequest request);
    UserMembership cancelMembership(Long userId);
    UserMembership getMembershipStatus(Long userId);

    // ── Orders (for tier evaluation demo) ─────────────────────────
    Order createOrder(CreateOrderRequest request);
    List<Order> getUserOrders(Long userId);
}
