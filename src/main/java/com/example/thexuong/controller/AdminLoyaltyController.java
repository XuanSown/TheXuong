package com.example.thexuong.controller;

import com.example.thexuong.entity.PointTier;
import com.example.thexuong.entity.PointTransaction;
import com.example.thexuong.entity.UserPoints;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.PointTierRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.UserPointsRepository;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    private final VoucherRepository voucherRepository;
    private final PointTierRepository pointTierRepository;
    private final UserPointsRepository userPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;

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

    // ============================================================
    // Batch 5: Admin Loyalty Report
    // ============================================================

    @GetMapping("/report")
    public String loyaltyReport(Model model) {
        // Total points stats
        long totalEarned = pointTransactionRepository.findAll().stream()
                .filter(t -> t.getType() == com.example.thexuong.entity.PointTransaction.Type.EARN)
                .mapToLong(PointTransaction::getPoints)
                .sum();
        long totalSpent = pointTransactionRepository.findAll().stream()
                .filter(t -> t.getType() == com.example.thexuong.entity.PointTransaction.Type.SPEND
                        || t.getType() == com.example.thexuong.entity.PointTransaction.Type.EXPIRE)
                .mapToLong(t -> Math.abs(t.getPoints()))
                .sum();

        // Top users by points
        List<UserPoints> topByPoints = userPointsRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getCurrentPoints(), a.getCurrentPoints()))
                .limit(10)
                .toList();

        // Top users by spent (need User join)
        // Simplified: just show points data
        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("topByPoints", topByPoints);
        model.addAttribute("totalUsersWithPoints", userPointsRepository.count());

        return "admin/loyalty-report";
    }
}
