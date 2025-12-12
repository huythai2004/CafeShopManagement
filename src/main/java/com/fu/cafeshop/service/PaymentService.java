package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Order;
import com.fu.cafeshop.entity.Payment;
import com.fu.cafeshop.repository.OrderRepository;
import com.fu.cafeshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional
    public Payment createPayment(Order order) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("CASH")
                .amount(order.getTotalAmount())
                .status("PENDING")
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment processPayment(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Payment already processed");
        }

        payment.setStatus("COMPLETED");
        payment.setPaidAt(LocalDateTime.now());

        // Update order status to DONE
        Order order = payment.getOrder();
        order.setStatus("DONE");
        order.setActualCompletionTime(LocalDateTime.now());
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void cancelPayment(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        payment.setStatus("CANCELLED");
        paymentRepository.save(payment);
    }

    public BigDecimal getTodayTotalPayments() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal total = paymentRepository.sumTodayPayments(startOfDay);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Payment> getCompletedPaymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findCompletedPaymentsByDateRange(start, end);
    }

    public long countPendingPayments() {
        return paymentRepository.countByStatus("PENDING");
    }

    public long countCompletedPayments() {
        return paymentRepository.countByStatus("COMPLETED");
    }
}

