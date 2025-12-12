package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.Customer;
import com.fu.cafeshop.entity.Order;
import com.fu.cafeshop.service.CartService;
import com.fu.cafeshop.service.CustomerService;
import com.fu.cafeshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerService customerService;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        if (cartService.isEmpty()) {
            return "redirect:/menu";
        }

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("total", cartService.getTotalAmount());
        return "checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(@RequestParam(required = false) String guestName,
                             @RequestParam(required = false) String guestPhone,
                             @RequestParam(required = false) String customerNotes,
                             RedirectAttributes redirectAttributes) {
        try {
            if (cartService.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/menu";
            }

            // Create or find customer if info provided
            Customer customer = null;
            if ((guestPhone != null && !guestPhone.isBlank()) || 
                (guestName != null && !guestName.isBlank())) {
                customer = customerService.createOrUpdateCustomer(guestName, guestPhone, null);
            }

            // Create order
            Order order = Order.builder()
                    .customer(customer)
                    .guestName(customer == null ? guestName : null)
                    .guestPhone(customer == null ? guestPhone : null)
                    .customerNotes(customerNotes)
                    .build();

            Order savedOrder = orderService.createOrder(order, cartService.toOrderCartItems());

            // Clear cart after successful order
            cartService.clearCart();

            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
            return "redirect:/order/status/" + savedOrder.getOrderNumber();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            return "redirect:/order/checkout";
        }
    }

    @GetMapping("/status/{orderNumber}")
    public String orderStatus(@PathVariable String orderNumber, Model model) {
        try {
            Order order = orderService.getOrderByNumber(orderNumber);
            model.addAttribute("order", order);
            return "order-status";
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng: " + orderNumber);
            return "order-status";
        }
    }

    @GetMapping("/track")
    public String trackOrder(@RequestParam(required = false) String orderNumber, Model model) {
        if (orderNumber != null && !orderNumber.isBlank()) {
            try {
                Order order = orderService.getOrderByNumber(orderNumber);
                model.addAttribute("order", order);
            } catch (Exception e) {
                model.addAttribute("error", "Không tìm thấy đơn hàng: " + orderNumber);
            }
        }
        return "order-track";
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        return cartService.getCartItemCount();
    }
}

