package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.Invoice;
import com.fu.cafeshop.entity.Order;
import com.fu.cafeshop.entity.User;
import com.fu.cafeshop.service.InvoiceService;
import com.fu.cafeshop.service.OrderService;
import com.fu.cafeshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/order")
@RequiredArgsConstructor
public class StaffOrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderWithItems(id);
        model.addAttribute("order", order);
        model.addAttribute("hasInvoice", invoiceService.hasInvoice(id));
        return "staff/order-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveOrder(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User staff = userService.getUserByUsername(authentication.getName());
            orderService.updateOrderStatus(id, "APPROVED", staff);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xác nhận!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/{id}/reject")
    public String rejectOrder(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã bị từ chối!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/{id}/cooking")
    public String startCooking(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User staff = userService.getUserByUsername(authentication.getName());
            orderService.updateOrderStatus(id, "COOKING", staff);
            redirectAttributes.addFlashAttribute("success", "Bắt đầu pha chế!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/{id}/done")
    public String completeOrder(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User staff = userService.getUserByUsername(authentication.getName());
            orderService.updateOrderStatus(id, "DONE", staff);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng hoàn thành!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @GetMapping("/{id}/invoice")
    public String viewInvoice(@PathVariable Long id,
                             Authentication authentication,
                             Model model) {
        Order order = orderService.getOrderWithItems(id);
        User staff = userService.getUserByUsername(authentication.getName());
        
        Invoice invoice = invoiceService.printInvoice(id, staff);
        
        model.addAttribute("order", order);
        model.addAttribute("invoice", invoice);
        return "staff/invoice";
    }

    @PostMapping("/{id}/print")
    public String printInvoice(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User staff = userService.getUserByUsername(authentication.getName());
            invoiceService.printInvoice(id, staff);
            redirectAttributes.addFlashAttribute("success", "Đã in hóa đơn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/order/" + id + "/invoice";
    }
}

