package com.firstclub.membership.service;

import com.firstclub.membership.dto.*;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.enums.MembershipStatus;
import com.firstclub.membership.exception.MembershipException;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of MembershipService.
 *
 * @Transactional on mutating methods ensures DB changes are atomic:
 * if any step fails, the whole operation is rolled back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    private final UserRepository userRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final UserMembershipRepository membershipRepository;
    private final OrderRepository orderRepository;
    private final TierEvaluationService tierEvaluationService;

    // ── Plan & Tier Discovery ──────────────────────────────────────────────────

    @Override
    public List<MembershipPlan> getAllActivePlans() {
        return planRepository.findByActiveTrue();
    }

    @Override
    public List<MembershipTier> getAllTiers() {
        return tierRepository.findAllByOrderByLevelAsc();
    }

    // ── User Management ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new MembershipException("User with email '" + request.getEmail() + "' already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .cohort(request.getCohort())
                .build();
        return userRepository.save(user);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    // ── Subscription Lifecycle ────────────────────────────────────────────────

    @Override
    @Transactional
    public UserMembership subscribe(SubscribeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        // Prevent duplicate active subscriptions
        membershipRepository.findByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE)
                .ifPresent(m -> {
                    throw new MembershipException(
                            "User already has an active membership. Cancel it before re-subscribing.");
                });

        MembershipPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.getPlanId()));

        if (!plan.isActive()) {
            throw new MembershipException("The selected plan is no longer available.");
        }

        MembershipTier tier = tierRepository.findById(request.getTierId())
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + request.getTierId()));

        // Check eligibility rules for the selected tier
        if (!tierEvaluationService.isEligibleForTier(user, tier)) {
            throw new MembershipException(
                    "User does not meet the eligibility criteria for tier: " + tier.getName());
        }

        LocalDateTime now = LocalDateTime.now();
        UserMembership membership = UserMembership.builder()
                .user(user)
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(now)
                .expiryDate(now.plusDays(plan.getDurationDays()))
                .build();

        log.info("User {} subscribed to plan '{}' at tier '{}'", user.getId(), plan.getName(), tier.getName());
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public UserMembership upgradeTier(Long userId, ChangeTierRequest request) {
        UserMembership membership = getActiveMembership(userId);
        MembershipTier newTier = getTierById(request.getTierId());
        MembershipTier currentTier = membership.getTier();

        // Must be a higher level
        if (newTier.getLevel() <= currentTier.getLevel()) {
            throw new MembershipException(
                    "Cannot upgrade to tier '" + newTier.getName() +
                    "'. It is not higher than current tier '" + currentTier.getName() + "'.");
        }

        User user = membership.getUser();
        if (!tierEvaluationService.isEligibleForTier(user, newTier)) {
            throw new MembershipException(
                    "User does not meet eligibility criteria for tier: " + newTier.getName());
        }

        membership.setTier(newTier);
        log.info("User {} upgraded from '{}' to '{}'", userId, currentTier.getName(), newTier.getName());
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public UserMembership downgradeTier(Long userId, ChangeTierRequest request) {
        UserMembership membership = getActiveMembership(userId);
        MembershipTier newTier = getTierById(request.getTierId());
        MembershipTier currentTier = membership.getTier();

        // Must be a lower level
        if (newTier.getLevel() >= currentTier.getLevel()) {
            throw new MembershipException(
                    "Cannot downgrade to tier '" + newTier.getName() +
                    "'. It is not lower than current tier '" + currentTier.getName() + "'.");
        }

        membership.setTier(newTier);
        log.info("User {} downgraded from '{}' to '{}'", userId, currentTier.getName(), newTier.getName());
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public UserMembership cancelMembership(Long userId) {
        UserMembership membership = getActiveMembership(userId);
        membership.setStatus(MembershipStatus.CANCELLED);
        log.info("User {} cancelled their membership", userId);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMembership getMembershipStatus(Long userId) {
        // Ensure user exists first
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return membershipRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No membership found for user: " + userId));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Order order = Order.builder()
                .user(user)
                .orderValue(request.getOrderValue())
                .build();

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return orderRepository.findByUserId(userId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private UserMembership getActiveMembership(Long userId) {
        return membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active membership found for user: " + userId));
    }

    private MembershipTier getTierById(Long tierId) {
        return tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierId));
    }
}
