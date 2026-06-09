package com.firstclub.membership.controller;

import com.firstclub.membership.dto.*;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for all Membership Program APIs.
 * Base path: /api/v1
 *
 * Versioning (/v1/) is a best practice — allows future breaking changes without disruption.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // ─────────────────────────────────────────────────────────────────────────
    // USER ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/users
     * Create a new user.
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = membershipService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    /**
     * GET /api/v1/users/{userId}
     * Get user details.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long userId) {
        User user = membershipService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAN & TIER DISCOVERY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/membership/plans
     * Returns all active plans (Monthly, Quarterly, Yearly).
     */
    @GetMapping("/membership/plans")
    public ResponseEntity<ApiResponse<List<MembershipPlan>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getAllActivePlans()));
    }

    /**
     * GET /api/v1/membership/tiers
     * Returns all membership tiers (Silver, Gold, Platinum) sorted by level.
     */
    @GetMapping("/membership/tiers")
    public ResponseEntity<ApiResponse<List<MembershipTier>>> getTiers() {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getAllTiers()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUBSCRIPTION LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/membership/subscribe
     * Subscribe a user to a plan + tier.
     * Body: { "userId": 1, "planId": 1, "tierId": 1 }
     */
    @PostMapping("/membership/subscribe")
    public ResponseEntity<ApiResponse<UserMembership>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        UserMembership membership = membershipService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscribed successfully", membership));
    }

    /**
     * GET /api/v1/membership/status/{userId}
     * Get a user's current membership status and expiry.
     */
    @GetMapping("/membership/status/{userId}")
    public ResponseEntity<ApiResponse<UserMembership>> getStatus(@PathVariable Long userId) {
        UserMembership membership = membershipService.getMembershipStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(membership));
    }

    /**
     * PUT /api/v1/membership/upgrade/{userId}
     * Upgrade the user's tier (e.g., Silver → Gold).
     * Body: { "tierId": 2 }
     */
    @PutMapping("/membership/upgrade/{userId}")
    public ResponseEntity<ApiResponse<UserMembership>> upgrade(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeTierRequest request) {
        UserMembership membership = membershipService.upgradeTier(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Tier upgraded successfully", membership));
    }

    /**
     * PUT /api/v1/membership/downgrade/{userId}
     * Downgrade the user's tier (e.g., Gold → Silver).
     * Body: { "tierId": 1 }
     */
    @PutMapping("/membership/downgrade/{userId}")
    public ResponseEntity<ApiResponse<UserMembership>> downgrade(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeTierRequest request) {
        UserMembership membership = membershipService.downgradeTier(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Tier downgraded successfully", membership));
    }

    /**
     * DELETE /api/v1/membership/cancel/{userId}
     * Cancel a user's active membership.
     */
    @DeleteMapping("/membership/cancel/{userId}")
    public ResponseEntity<ApiResponse<UserMembership>> cancel(@PathVariable Long userId) {
        UserMembership membership = membershipService.cancelMembership(userId);
        return ResponseEntity.ok(ApiResponse.success("Membership cancelled", membership));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ORDERS (for demo / tier evaluation)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/orders
     * Place an order for a user. Orders feed into the tier evaluation logic.
     * Body: { "userId": 1, "orderValue": 1500.00 }
     */
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = membershipService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", order));
    }

    /**
     * GET /api/v1/orders/user/{userId}
     * Get all orders for a user.
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getUserOrders(userId)));
    }
}
