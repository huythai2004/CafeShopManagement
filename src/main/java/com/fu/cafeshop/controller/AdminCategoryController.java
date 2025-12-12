package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.Category;
import com.fu.cafeshop.service.CategoryService;
import com.fu.cafeshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/category/list";
    }

    @GetMapping("/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/create";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category,
                                RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("success", "Tạo danh mục thành công!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/category/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                @ModelAttribute Category categoryDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, categoryDetails);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            long productCount = productService.countProductsByCategory(id);
            if (productCount > 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể xóa danh mục có " + productCount + " sản phẩm!");
            } else {
                categoryService.deleteCategory(id);
                redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/view/{id}")
    public String viewCategory(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        model.addAttribute("products", productService.getProductsByCategory(id));
        return "admin/category/view";
    }
}

