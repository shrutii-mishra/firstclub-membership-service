package com.firstclub.membership.repository;

import com.firstclub.membership.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    // Count orders placed by a user after a given date
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.createdAt >= :from")
    Long countByUserIdSince(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    // Sum of all order values by a user after a given date
    @Query("SELECT COALESCE(SUM(o.orderValue), 0) FROM Order o WHERE o.user.id = :userId AND o.createdAt >= :from")
    BigDecimal sumOrderValueByUserIdSince(@Param("userId") Long userId, @Param("from") LocalDateTime from);
}
