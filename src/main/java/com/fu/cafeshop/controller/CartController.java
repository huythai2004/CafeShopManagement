package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.Product;
import com.fu.cafeshop.entity.ProductModifier;
import com.fu.cafeshop.service.CartService;
import com.fu.cafeshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItemsMap());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("total", cartService.getTotalAmount());
        model.addAttribute("itemCount", cartService.getCartItemCount());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam(defaultValue = "1") int quantity,
                           @RequestParam(required = false) List<Long> modifierIds,
                           RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(productId);
            
            List<ProductModifier> selectedModifiers = new ArrayList<>();
            if (modifierIds != null && !modifierIds.isEmpty()) {
                for (Long modifierId : modifierIds) {
                    productService.getProductModifiers(productId).stream()
                            .filter(m -> m.getId().equals(modifierId))
                            .findFirst()
                            .ifPresent(selectedModifiers::add);
                }
            }

            cartService.addToCart(product, quantity, selectedModifiers);
            redirectAttributes.addFlashAttribute("success", "Đã thêm " + product.getName() + " vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/menu";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam String itemKey,
                            @RequestParam int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(itemKey, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam String itemKey,
                                RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(itemKey);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        cartService.clearCart();
        redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng!");
        return "redirect:/cart";
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        return cartService.getCartItemCount();
    }
}

