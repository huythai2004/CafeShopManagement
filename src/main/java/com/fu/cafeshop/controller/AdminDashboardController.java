package com.fu.cafeshop.controller;

import com.fu.cafeshop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Stats
        model.addAttribute("totalProducts", productService.countAvailableProducts());
        model.addAttribute("totalCategories", categoryService.countCategories());
        model.addAttribute("totalStaff", userService.countByRole("STAFF"));
        
        // Order stats
        model.addAttribute("pendingOrders", orderService.countByStatus("PENDING"));
        model.addAttribute("todayOrders", orderService.countTodayCompletedOrders());
        model.addAttribute("todayRevenue", orderService.getTodayRevenue());

        // Recent orders
        model.addAttribute("recentOrders", orderService.getRecentOrders());

        return "admin/dashboard";
    }
}
