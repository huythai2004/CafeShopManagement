package com.fu.cafeshop.controller;

import com.fu.cafeshop.entity.CafeTable;
import com.fu.cafeshop.service.CafeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tables")
@RequiredArgsConstructor
public class AdminTableController {

    private final CafeTableService cafeTableService;

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute("tables", cafeTableService.getAllTables());
        model.addAttribute("activeCount", cafeTableService.countActiveTables());
        model.addAttribute("totalCount", cafeTableService.countTables());
        return "admin/table/list";
    }

    @GetMapping("/create")
    public String createTableForm(Model model) {
        model.addAttribute("table", new CafeTable());
        return "admin/table/create";
    }

    @PostMapping("/create")
    public String createTable(@ModelAttribute CafeTable table,
                             RedirectAttributes redirectAttributes) {
        try {
            cafeTableService.createTable(table);
            redirectAttributes.addFlashAttribute("success", "Thêm bàn mới thành công!");
            return "redirect:/admin/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/tables/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editTableForm(@PathVariable Long id, Model model) {
        model.addAttribute("table", cafeTableService.getTableById(id));
        return "admin/table/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTable(@PathVariable Long id,
                             @ModelAttribute CafeTable tableDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            cafeTableService.updateTable(id, tableDetails);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bàn thành công!");
            return "redirect:/admin/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/tables/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTable(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            cafeTableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bàn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/toggle/{id}")
    public String toggleTableStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            cafeTableService.toggleTableStatus(id);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái bàn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }
}


