package com.firstclub.membership.repository;

import com.firstclub.membership.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    Optional<MembershipTier> findByName(String name);
    List<MembershipTier> findAllByOrderByLevelAsc(); // Returns tiers sorted Silver → Platinum
}
