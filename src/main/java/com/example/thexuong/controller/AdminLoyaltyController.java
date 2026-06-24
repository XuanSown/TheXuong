package com.example.thexuong.controller;

import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller cho admin CRUD voucher catalog (Thymeleaf).
 * - GET /admin/loyalty/vouchers        : list + form
 * - POST /admin/loyalty/vouchers/save  : tạo/sửa
 * - POST /admin/loyalty/vouchers/{id}/lock   : khoá voucher
 * - POST /admin/loyalty/vouchers/{id}/delete : xoá
 *
 * REST API ở AdminLoyaltyApiController (Task 2.14).
 */
@Controller
@RequestMapping("/admin/loyalty/vouchers")
@RequiredArgsConstructor
public class AdminLoyaltyController {

    @Autowired
    private final VoucherRepository voucherRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherRepository.findAll());
        model.addAttribute("formVoucher", new Voucher());
        return "admin/loyalty-vouchers";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("formVoucher") Voucher form,
                       RedirectAttributes redirectAttributes) {
        try {
            if (form.getId() == null) {
                form.setStatus(Voucher.Status.ACTIVE);
            } else {
                Voucher existing = voucherRepository.findById(form.getId()).orElse(null);
                if (existing != null) {
                    existing.setDiscountAmount(form.getDiscountAmount());
                    existing.setRequiredPoints(form.getRequiredPoints());
                    existing.setMinOrderAmount(form.getMinOrderAmount());
                    existing.setVipOnly(form.getVipOnly());
                    existing.setStatus(form.getStatus());
                    voucherRepository.save(existing);
                    redirectAttributes.addFlashAttribute("success", "Cập nhật voucher thành công.");
                    return "redirect:/admin/loyalty/vouchers";
                }
            }
            voucherRepository.save(form);
            redirectAttributes.addFlashAttribute("success", "Tạo voucher mới thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/loyalty/vouchers";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v != null) {
            v.setStatus(v.getStatus() == Voucher.Status.LOCKED ? Voucher.Status.ACTIVE : Voucher.Status.LOCKED);
            voucherRepository.save(v);
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái voucher.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá voucher.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xoá voucher (có thể do user đã sở hữu). Hãy khoá thay vì xoá.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }
}
