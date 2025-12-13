package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.*;
import com.fu.cafeshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    private static final AtomicLong orderSequence = new AtomicLong(1);

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders(Arrays.asList("PENDING", "APPROVED", "COOKING"));
    }

    public List<Order> getActiveOrders() {
        return orderRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList("PENDING", "APPROVED", "COOKING", "IN_PROGRESS"));
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    public Order getOrderWithItems(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(Order order, List<CartItem> cartItems) {
        // Generate order number
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        order.setStatus("PENDING");
        
        // Initialize default values if null (builder doesn't apply field defaults)
        if (order.getTaxAmount() == null) order.setTaxAmount(BigDecimal.ZERO);
        if (order.getDiscountAmount() == null) order.setDiscountAmount(BigDecimal.ZERO);

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getDefaultPrice());
            // Handle null modifiersPrice
            BigDecimal modPrice = cartItem.getModifiersPrice() != null ? cartItem.getModifiersPrice() : BigDecimal.ZERO;
            orderItem.setModifiersPrice(modPrice);
            orderItem.calculateTotalPrice();

            subtotal = subtotal.add(orderItem.getTotalPrice());
            orderItems.add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getTaxAmount()).subtract(order.getDiscountAmount()));
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Create pending payment
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod("CASH")
                .amount(savedOrder.getTotalAmount())
                .status("PENDING")
                .build();
        paymentRepository.save(payment);

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus, User staff) {
        Order order = getOrderById(orderId);
        String oldStatus = order.getStatus();

        // Validate status transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);

        if ("APPROVED".equals(newStatus)) {
            order.setApprovedByStaff(staff);
        }

        if ("DONE".equals(newStatus)) {
            order.setActualCompletionTime(LocalDateTime.now());
            // Mark payment as completed
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            if (payment != null) {
                payment.setStatus("COMPLETED");
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if ("DONE".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel completed order");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // Cancel payment
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null) {
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
        }
    }

    private boolean isValidStatusTransition(String from, String to) {
        return switch (from) {
            case "PENDING" -> "APPROVED".equals(to) || "CANCELLED".equals(to);
            case "APPROVED" -> "COOKING".equals(to) || "CANCELLED".equals(to);
            case "COOKING" -> "DONE".equals(to) || "IN_PROGRESS".equals(to);
            case "IN_PROGRESS" -> "DONE".equals(to);
            default -> false;
        };
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = orderSequence.getAndIncrement();
        return String.format("%s-%06d", datePrefix, seq);
    }

    // Statistics
    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    public long countTodayCompletedOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return orderRepository.countTodayCompletedOrders(startOfDay);
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal revenue = orderRepository.sumTodayRevenue(startOfDay);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal getRevenueByDateRange(LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = orderRepository.sumRevenueByDateRange(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByDateRange(start, end);
    }

    // Inner class for cart items
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CartItem {
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal modifiersPrice;
        private List<String> modifierNames;

        public BigDecimal getTotalPrice() {
            return unitPrice.add(modifiersPrice).multiply(new BigDecimal(quantity));
        }
    }
}

