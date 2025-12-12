package com.fu.cafeshop.controller;

import com.fu.cafeshop.service.OrderService;
import com.fu.cafeshop.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final OrderService orderService;

    @GetMapping
    public String reports(Model model) {
        model.addAttribute("stats", reportService.getDashboardStats());
        model.addAttribute("todayReport", reportService.getDailyReport(LocalDate.now()));
        return "admin/report/index";
    }

    @GetMapping("/daily")
    public String dailyReport(@RequestParam(required = false) 
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        if (date == null) {
            date = LocalDate.now();
        }
        model.addAttribute("report", reportService.getDailyReport(date));
        model.addAttribute("selectedDate", date);
        return "admin/report/daily";
    }

    @GetMapping("/monthly")
    public String monthlyReport(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Model model) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        model.addAttribute("report", reportService.getMonthlyReport(year, month));
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        return "admin/report/monthly";
    }

    @GetMapping("/bestsellers")
    public String bestSellers(@RequestParam(required = false) 
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) 
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        model.addAttribute("bestSellers", reportService.getBestSellers(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/report/bestsellers";
    }

    @GetMapping("/orders")
    public String orderHistory(@RequestParam(required = false) String status,
                              Model model) {
        if (status != null && !status.isBlank()) {
            model.addAttribute("orders", orderService.getOrdersByStatus(status));
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("orders", orderService.getAllOrders());
        }
        return "admin/report/orders";
    }
}

