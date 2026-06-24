package com.example.thexuong.controller;

<<<<<<< HEAD
import com.example.thexuong.entity.Voucher;
=======
import com.example.thexuong.entity.PointTier;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.PointTierRepository;
>>>>>>> feat/batch-4-tier-vip
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
<<<<<<< HEAD
 * Controller cho admin CRUD voucher catalog (Thymeleaf).
 * - GET /admin/loyalty/vouchers        : list + form
 * - POST /admin/loyalty/vouchers/save  : tạo/sửa
 * - POST /admin/loyalty/vouchers/{id}/lock   : khoá voucher
 * - POST /admin/loyalty/vouchers/{id}/delete : xoá
=======
 * Controller cho admin CRUD loyalty (Thymeleaf).
 *
 * Endpoints:
 * - GET  /admin/loyalty/vouchers         : list + form catalog voucher
 * - POST /admin/loyalty/vouchers/save   : tạo/sửa
 * - POST /admin/loyalty/vouchers/{id}/lock    : khoá voucher
 * - POST /admin/loyalty/vouchers/{id}/delete  : xoá
 * - GET  /admin/loyalty/config          : sửa tier thresholds (Task 4.10)
 * - POST /admin/loyalty/config/save     : lưu tier thresholds
>>>>>>> feat/batch-4-tier-vip
 *
 * REST API ở AdminLoyaltyApiController (Task 2.14).
 */
@Controller
<<<<<<< HEAD
@RequestMapping("/admin/loyalty/vouchers")
=======
@RequestMapping("/admin/loyalty")
>>>>>>> feat/batch-4-tier-vip
@RequiredArgsConstructor
public class AdminLoyaltyController {

    @Autowired
    private final VoucherRepository voucherRepository;
<<<<<<< HEAD

    @GetMapping
    public String list(Model model) {
=======
    @Autowired
    private final PointTierRepository pointTierRepository;

    // ============================================================
    // Voucher Catalog CRUD
    // ============================================================

    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
>>>>>>> feat/batch-4-tier-vip
        model.addAttribute("vouchers", voucherRepository.findAll());
        model.addAttribute("formVoucher", new Voucher());
        return "admin/loyalty-vouchers";
    }

<<<<<<< HEAD
    @PostMapping("/save")
    public String save(@ModelAttribute("formVoucher") Voucher form,
                       RedirectAttributes redirectAttributes) {
=======
    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute("formVoucher") Voucher form,
                              RedirectAttributes redirectAttributes) {
>>>>>>> feat/batch-4-tier-vip
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

<<<<<<< HEAD
    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
=======
    @PostMapping("/vouchers/{id}/lock")
    public String lockVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
>>>>>>> feat/batch-4-tier-vip
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v != null) {
            v.setStatus(v.getStatus() == Voucher.Status.LOCKED ? Voucher.Status.ACTIVE : Voucher.Status.LOCKED);
            voucherRepository.save(v);
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái voucher.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }

<<<<<<< HEAD
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
=======
    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
>>>>>>> feat/batch-4-tier-vip
        try {
            voucherRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá voucher.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xoá voucher (có thể do user đã sở hữu). Hãy khoá thay vì xoá.");
        }
        return "redirect:/admin/loyalty/vouchers";
    }
<<<<<<< HEAD
=======

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
>>>>>>> feat/batch-4-tier-vip
}
