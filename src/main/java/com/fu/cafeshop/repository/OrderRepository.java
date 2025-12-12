package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders(@Param("statuses") List<String> statuses);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.status = 'DONE' AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findCompletedOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DONE' AND o.createdAt >= :startOfDay")
    long countTodayCompletedOrders(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DONE' AND o.createdAt >= :startOfDay")
    java.math.BigDecimal sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DONE' AND o.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    List<Order> findTop10ByOrderByCreatedAtDesc();
}

