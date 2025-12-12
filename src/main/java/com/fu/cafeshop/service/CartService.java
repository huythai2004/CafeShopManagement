package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Product;
import com.fu.cafeshop.entity.ProductModifier;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.*;

@Service
@SessionScope
public class CartService {

    private final Map<String, CartItem> cartItems = new LinkedHashMap<>();

    public void addToCart(Product product, int quantity, List<ProductModifier> selectedModifiers) {
        String key = generateKey(product.getId(), selectedModifiers);

        if (cartItems.containsKey(key)) {
            CartItem existingItem = cartItems.get(key);
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            BigDecimal modifiersPrice = selectedModifiers.stream()
                    .map(ProductModifier::getPriceDelta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<String> modifierNames = selectedModifiers.stream()
                    .map(ProductModifier::getName)
                    .toList();

            CartItem item = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .unitPrice(product.getDefaultPrice())
                    .quantity(quantity)
                    .modifiersPrice(modifiersPrice)
                    .modifierNames(modifierNames)
                    .build();

            cartItems.put(key, item);
        }
    }

    public void updateQuantity(String key, int quantity) {
        if (cartItems.containsKey(key)) {
            if (quantity <= 0) {
                cartItems.remove(key);
            } else {
                cartItems.get(key).setQuantity(quantity);
            }
        }
    }

    public void removeFromCart(String key) {
        cartItems.remove(key);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public Map<String, CartItem> getCartItemsMap() {
        return new LinkedHashMap<>(cartItems);
    }

    public int getCartItemCount() {
        return cartItems.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getSubtotal() {
        return cartItems.values().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmount() {
        return getSubtotal();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private String generateKey(Long productId, List<ProductModifier> modifiers) {
        StringBuilder key = new StringBuilder(productId.toString());
        if (modifiers != null && !modifiers.isEmpty()) {
            modifiers.stream()
                    .map(ProductModifier::getId)
                    .sorted()
                    .forEach(id -> key.append("-").append(id));
        }
        return key.toString();
    }

    public List<OrderService.CartItem> toOrderCartItems() {
        return cartItems.values().stream()
                .map(item -> new OrderService.CartItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getModifiersPrice(),
                        item.getModifierNames()
                ))
                .toList();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private Long productId;
        private String productName;
        private String imageUrl;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal modifiersPrice;
        private List<String> modifierNames;

        public BigDecimal getTotalPrice() {
            return unitPrice.add(modifiersPrice != null ? modifiersPrice : BigDecimal.ZERO)
                    .multiply(new BigDecimal(quantity));
        }

        public BigDecimal getItemPrice() {
            return unitPrice.add(modifiersPrice != null ? modifiersPrice : BigDecimal.ZERO);
        }
    }
}

