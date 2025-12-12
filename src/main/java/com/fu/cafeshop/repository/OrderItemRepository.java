package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.productName, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.status = 'DONE' AND o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.productName ORDER BY totalQty DESC")
    List<Object[]> findBestSellersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalQty, SUM(oi.totalPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o WHERE o.status = 'DONE' " +
           "GROUP BY oi.product.id, oi.productName ORDER BY totalQty DESC")
    List<Object[]> findProductSalesReport();

    void deleteByOrderId(Long orderId);
}

