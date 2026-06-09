package com.firstclub.membership.repository;

import com.firstclub.membership.entity.UserMembership;
import com.firstclub.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUserId(Long userId);
    Optional<UserMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);
}
