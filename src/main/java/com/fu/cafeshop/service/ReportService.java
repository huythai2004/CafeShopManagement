package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Order;
import com.fu.cafeshop.repository.OrderItemRepository;
import com.fu.cafeshop.repository.OrderRepository;
import com.fu.cafeshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public Map<String, Object> getDailyReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Map<String, Object> report = new HashMap<>();
        
        List<Order> orders = orderRepository.findCompletedOrdersByDateRange(startOfDay, endOfDay);
        BigDecimal revenue = orderRepository.sumRevenueByDateRange(startOfDay, endOfDay);

        report.put("date", date);
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        report.put("orders", orders);

        return report;
    }

    public Map<String, Object> getMonthlyReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Object> report = new HashMap<>();

        List<Order> orders = orderRepository.findCompletedOrdersByDateRange(startDateTime, endDateTime);
        BigDecimal revenue = orderRepository.sumRevenueByDateRange(startDateTime, endDateTime);

        report.put("year", year);
        report.put("month", month);
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);

        // Daily breakdown
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            LocalDateTime dayStart = d.atStartOfDay();
            LocalDateTime dayEnd = d.atTime(LocalTime.MAX);
            BigDecimal dayRevenue = orderRepository.sumRevenueByDateRange(dayStart, dayEnd);
            
            Map<String, Object> dayReport = new HashMap<>();
            dayReport.put("date", d);
            dayReport.put("revenue", dayRevenue != null ? dayRevenue : BigDecimal.ZERO);
            dailyData.add(dayReport);
        }
        report.put("dailyData", dailyData);

        return report;
    }

    public List<Map<String, Object>> getBestSellers(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = orderItemRepository.findBestSellersByDateRange(start, end);
        List<Map<String, Object>> bestSellers = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", row[0]);
            item.put("totalQuantity", row[1]);
            bestSellers.add(item);
        }

        return bestSellers;
    }

    public List<Map<String, Object>> getProductSalesReport() {
        List<Object[]> results = orderItemRepository.findProductSalesReport();
        List<Map<String, Object>> salesReport = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", row[0]);
            item.put("productName", row[1]);
            item.put("totalQuantity", row[2]);
            item.put("totalRevenue", row[3]);
            salesReport.add(item);
        }

        return salesReport;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        
        // Today's stats
        stats.put("todayOrders", orderRepository.countTodayCompletedOrders(startOfDay));
        stats.put("todayRevenue", getTodayRevenue());
        
        // Order status counts
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("cookingOrders", orderRepository.countByStatus("COOKING"));
        stats.put("completedOrders", orderRepository.countByStatus("DONE"));

        return stats;
    }

    private BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal revenue = orderRepository.sumTodayRevenue(startOfDay);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}

