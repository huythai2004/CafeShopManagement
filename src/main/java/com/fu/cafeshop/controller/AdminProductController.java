package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.Product;
import com.fu.cafeshop.entity.ProductModifier;
import com.fu.cafeshop.service.CategoryService;
import com.fu.cafeshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(required = false) Long categoryId, Model model) {
        if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId));
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product/list";
    }

    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/product/create";
    }

    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product,
                               @RequestParam Long categoryId,
                               RedirectAttributes redirectAttributes) {
        try {
            product.setCategory(categoryService.getCategoryById(categoryId));
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Tạo sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductWithModifiers(id));
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/product/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                               @ModelAttribute Product productDetails,
                               @RequestParam Long categoryId,
                               RedirectAttributes redirectAttributes) {
        try {
            productDetails.setCategory(categoryService.getCategoryById(categoryId));
            productService.updateProduct(id, productDetails);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    @PostMapping("/toggle-availability/{id}")
    public String toggleAvailability(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            productService.toggleAvailability(id);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductWithModifiers(id));
        return "admin/product/view";
    }

    // Modifier management
    @PostMapping("/{productId}/modifiers/add")
    public String addModifier(@PathVariable Long productId,
                             @RequestParam String name,
                             @RequestParam BigDecimal priceDelta,
                             @RequestParam(defaultValue = "false") boolean isDefault,
                             RedirectAttributes redirectAttributes) {
        try {
            ProductModifier modifier = ProductModifier.builder()
                    .name(name)
                    .priceDelta(priceDelta)
                    .isDefault(isDefault)
                    .build();
            productService.addModifier(productId, modifier);
            redirectAttributes.addFlashAttribute("success", "Đã thêm tùy chọn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/edit/" + productId;
    }

    @PostMapping("/{productId}/modifiers/delete/{modifierId}")
    public String deleteModifier(@PathVariable Long productId,
                                @PathVariable Long modifierId,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteModifier(modifierId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tùy chọn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/edit/" + productId;
    }
}

