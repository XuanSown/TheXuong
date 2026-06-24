package com.example.thexuong.controller;

import com.example.thexuong.entity.PointTier;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.PointTierRepository;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller cho admin CRUD loyalty (Thymeleaf).
 *
 * Endpoints:
 * - GET  /admin/loyalty/vouchers         : list + form catalog voucher
 * - POST /admin/loyalty/vouchers/save   : tạo/sửa
 * - POST /admin/loyalty/vouchers/{id}/lock    : khoá voucher
 * - POST /admin/loyalty/vouchers/{id}/delete  : xoá
 * - GET  /admin/loyalty/config          : sửa tier thresholds (Task 4.10)
 * - POST /admin/loyalty/config/save     : lưu tier thresholds
 *
 * REST API ở AdminLoyaltyApiController (Task 2.14).
 */
@Controller
@RequestMapping("/admin/loyalty")
@RequiredArgsConstructor
public class AdminLoyaltyController {

    @Autowired
    private final VoucherRepository voucherRepository;

    @Autowired
    private final PointTierRepository pointTierRepository;

    // ============================================================
    // Voucher Catalog CRUD
    // ============================================================

    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherRepository.findAll());
        model.addAttribute("formVoucher", new Voucher());
        return "admin/loyalty-vouchers";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute("formVoucher") Voucher form,
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

    @PostMapping("/vouchers/{id}/lock")
    public String lockVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v != null) {
            v.setStatus(v.getStatus() == Voucher.Status.LOCKED ? Voucher.Status.ACTIVE : Voucher.Status.LOCKED);
            voucherRepository.save(v);
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái voucher.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá voucher.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xoá voucher (có thể do user đã sở hữu). Hãy khoá thay vì xoá.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }

    // ============================================================
    // Task 4.10-4.11: Tier Config
    // ============================================================

    @GetMapping("/config")
    public String configPage(Model model) {
        model.addAttribute("tiers", pointTierRepository.findAllByOrderByMinTotalSpentAsc());
        return "admin/loyalty-config";
    }

    @PostMapping("/config/save")
    public String saveConfig(@RequestParam("vipMinSpent") String vipMinSpent,
                             @RequestParam("vipMinPoints") Integer vipMinPoints,
                             RedirectAttributes redirectAttributes) {
        try {
            PointTier vip = pointTierRepository.findByCode("VIP").orElse(null);
            if (vip == null) {
                vip = PointTier.builder()
                        .code("VIP")
                        .name("Khách hàng VIP")
                        .minTotalSpent(new java.math.BigDecimal(vipMinSpent))
                        .minTotalPoints(vipMinPoints)
                        .benefits("{\"vipBonus\": true, \"freeShipping\": true}")
                        .build();
            } else {
                vip.setMinTotalSpent(new java.math.BigDecimal(vipMinSpent));
                vip.setMinTotalPoints(vipMinPoints);
            }
            pointTierRepository.save(vip);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tier thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/loyalty/config";
    }
}
