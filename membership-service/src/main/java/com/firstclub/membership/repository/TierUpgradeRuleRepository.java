package com.firstclub.membership.repository;

import com.firstclub.membership.entity.TierUpgradeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TierUpgradeRuleRepository extends JpaRepository<TierUpgradeRule, Long> {
    List<TierUpgradeRule> findByTierIdAndActiveTrue(Long tierId);
}
