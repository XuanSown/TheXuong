package com.example.thexuong.controller;

import com.example.thexuong.entity.PointTransaction;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.PointService;
import com.example.thexuong.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller cho customer loyalty pages (Thymeleaf).
 * Endpoints:
 * - GET /loyalty        : số dư điểm + lịch sử
 * - GET /loyalty/redeem : grid 6 mệnh giá
 * - POST /loyalty/redeem: redeem voucher
 * - GET /my-vouchers    : 3 tab UNUSED/USED/EXPIRED
 *
 * REST API sẽ thêm ở LoyaltyApiController (Task 2.14, 2.17).
 */
@Controller
@RequiredArgsConstructor
public class LoyaltyController {

    @Autowired
    private final PointService pointService;
    @Autowired
    private final VoucherService voucherService;
    @Autowired
    private final UserRepository userRepository;

    @GetMapping("/loyalty")
    public String loyaltyIndex(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = resolveUser(principal);
        if (user == null) return "redirect:/login";

        int currentPoints = pointService.getCurrentPoints(user.getId());
        List<PointTransaction> history = pointService.getHistory(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("currentPoints", currentPoints);
        model.addAttribute("history", history);
        // TODO Batch 4: user.getTierCode() (sẽ thêm field tierCode vào User entity)
        model.addAttribute("tier", "THUONG");
        return "loyalty/index";
    }

    @GetMapping("/loyalty/redeem")
    public String redeemPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = resolveUser(principal);
        if (user == null) return "redirect:/login";

        List<Voucher> catalog = voucherService.getActiveCatalog();
        int currentPoints = pointService.getCurrentPoints(user.getId());

        model.addAttribute("catalog", catalog);
        model.addAttribute("currentPoints", currentPoints);
        return "loyalty/redeem";
    }

    @PostMapping("/loyalty/redeem")
    public String redeemVoucher(@RequestParam("voucherId") Long voucherId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User user = resolveUser(principal);
        if (user == null) return "redirect:/login";

        try {
            UserVoucher uv = voucherService.redeemVoucher(user.getId(), voucherId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đổi voucher thành công! Mã của anh/chị: " + uv.getCode()
                            + " (hết hạn " + uv.getExpiresAt().toLocalDate() + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-vouchers";
    }

    @GetMapping("/my-vouchers")
    public String myVouchers(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = resolveUser(principal);
        if (user == null) return "redirect:/login";

        List<UserVoucher> all = voucherService.getUserVouchers(user.getId());
        long unusedCount = all.stream().filter(v -> v.getStatus() == UserVoucher.Status.UNUSED).count();
        long usedCount = all.stream().filter(v -> v.getStatus() == UserVoucher.Status.USED).count();
        long expiredCount = all.stream().filter(v -> v.getStatus() == UserVoucher.Status.EXPIRED).count();

        model.addAttribute("vouchers", all);
        model.addAttribute("unusedCount", unusedCount);
        model.addAttribute("usedCount", usedCount);
        model.addAttribute("expiredCount", expiredCount);
        return "my-vouchers";
    }

    private User resolveUser(Principal principal) {
        String identifier = principal.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));
    }
}
