package com.fu.cafeshop.controller;

import com.fu.cafeshop.service.OrderService;
import com.fu.cafeshop.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final OrderService orderService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Active orders for staff to process
        model.addAttribute("pendingOrders", orderService.getOrdersByStatus("PENDING"));
        model.addAttribute("approvedOrders", orderService.getOrdersByStatus("APPROVED"));
        model.addAttribute("cookingOrders", orderService.getOrdersByStatus("COOKING"));
        
        // Stats
        model.addAttribute("stats", reportService.getDashboardStats());
        
        return "staff/dashboard";
    }

    @GetMapping("/orders")
    public String allOrders(Model model) {
        model.addAttribute("orders", orderService.getActiveOrders());
        return "staff/orders";
    }

    @GetMapping("/completed")
    public String completedOrders(Model model) {
        model.addAttribute("orders", orderService.getOrdersByStatus("DONE"));
        return "staff/completed";
    }
}

