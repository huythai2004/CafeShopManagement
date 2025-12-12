package com.fu.cafeshop.controller;

import com.fu.cafeshop.service.CategoryService;
import com.fu.cafeshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public String showMenu(@RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String search,
                          Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        
        if (search != null && !search.isBlank()) {
            model.addAttribute("products", productService.searchProducts(search));
            model.addAttribute("searchKeyword", search);
        } else if (categoryId != null) {
            model.addAttribute("products", productService.getAvailableProductsByCategory(categoryId));
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            model.addAttribute("products", productService.getAvailableProducts());
        }

        return "menu";
    }

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductWithModifiers(id));
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "product-detail";
    }
}

