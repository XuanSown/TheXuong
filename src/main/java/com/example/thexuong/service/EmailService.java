package com.example.thexuong.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail){
        String subject = "Welcome to The Xuong";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;">
                                <h2 style="color: #FFC107; text-align: center;">THE XUONG SPORT</h2>
                                <p>Xin chào,</p>
                                <p>Cảm ơn bạn đã đăng ký nhận bản tin từ <strong>The Xuong</strong>.</p>
                                <p>Bạn sẽ là người đầu tiên nhận được thông báo về:</p>
                                <ul>
                                    <li>Các bộ sưu tập mới nhất.</li>
                                    <li>Chương trình khuyến mãi độc quyền.</li>
                                    <li>Mã giảm giá dành riêng cho thành viên.</li>
                                </ul>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="https://thexuong.xuansown.id.vn/products" style="background-color: #000; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
                                </div>
                                <p style="font-size: 12px; color: #666; text-align: center;">
                                    Nếu bạn không yêu cầu email này, vui lòng bỏ qua.<br>
                                    © 2026 The Xuong Sport. All rights reserved.
                                </p>
                </div>
                """;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNewPassword(String toEmail, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("TheXuong");
        message.setTo(toEmail);
        message.setSubject("Cấp lại mật khẩu mới - TheXuong");
        message.setText("Chào bạn,\n\n"
                + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay lập tức để bảo mật tài khoản.\n\n"
                + "Trân trọng,\nTheXuong Team");

        mailSender.send(message);
    }

    // ============================================================
    // Password Reset (ForgotPasswordController)
    // ============================================================

    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        String subject = "Đặt lại mật khẩu - TheXuong";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #333; text-align: center;">THE XUONG SPORT</h2>
                    <p>Xin chào,</p>
                    <p>Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản <strong>%s</strong>.</p>
                    <p>Nhấn vào nút bên dưới để đặt mật khẩu mới:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #000; color: #fff; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 4px;">ĐẶT LẠI MẬT KHẨU</a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="background: #f5f5f5; padding: 10px; word-break: break-all; border-radius: 4px;">%s</p>
                    <p><strong>Lưu ý:</strong> Link này có hiệu lực trong <strong>2 giờ</strong>. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>
                    <p style="font-size: 12px; color: #666; text-align: center;">
                        Nếu bạn gặp vấn đề, liên hệ support@thexuong.com.<br>
                        © 2026 The Xuong Sport. All rights reserved.
                    </p>
                </div>
                """.formatted(toEmail, resetUrl, resetUrl);
        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendPasswordChangedConfirmation(String toEmail) {
        String subject = "Mật khẩu đã được thay đổi - TheXuong";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #4CAF50; text-align: center;">THE XUONG SPORT</h2>
                    <p>Xin chào,</p>
                    <p>Mật khẩu của tài khoản bạn vừa được <strong style="color: #4CAF50;">thay đổi thành công</strong>.</p>
                    <p>Nếu bạn thực hiện thay đổi này, mọi thứ đều ổn. Nếu <strong>KHÔNG</strong> phải bạn, vui lòng:</p>
                    <ol>
                        <li>Ngay lập tức đặt lại mật khẩu</li>
                        <li>Liên hệ với chúng tôi qua support@thexuong.com</li>
                    </ol>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/forgot-password" style="background-color: #f44336; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">ĐẶT LẠI MẬT KHẨU KHẨN CẤP</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">
                        © 2026 The Xuong Sport. All rights reserved.
                    </p>
                </div>
                """;
        sendHtmlEmail(toEmail, subject, html);
    }

    // ============================================================
    // Batch 4: Tier email notifications
    // ============================================================

    /**
     * Task 4.21: Gửi email chúc mừng khi user vừa lên hạng mới.
     * Gọi từ OrderService.confirmReceived (sau khi upgrade tier thành công).
     */
    public void sendVipWelcome(String toEmail, String fullName) {
        String subject = "Chúc mừng anh/chị đã thăng hạng thành viên! - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #d4af37; text-align: center;">THE XUONG - CHÚC MỪNG!</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúc mừng anh/chị đã chính thức được <strong style="color: #d4af37;">thăng hạng thành viên</strong> tại TheXuong!</p>
                    <h3>Quyền lợi hạng mới:</h3>
                    <ul>
                        <li>✨ Tận hưởng các ưu đãi đặc quyền theo cấp bậc</li>
                        <li>🔥 Nhận Voucher thưởng thăng hạng (nếu có)</li>
                        <li>💎 Ưu tiên hỗ trợ đặc biệt</li>
                    </ul>
                    <p>Để giữ hạng, anh/chị cần duy trì tổng chi tiêu hoặc điểm tích lũy theo quy định của hạng. Hệ thống sẽ tự động đánh giá lại vào ngày 1 hàng tháng.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/loyalty" style="background-color: #d4af37; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">XEM ĐIỂM THƯỞNG</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport. All rights reserved.</p>
                </div>
                """.formatted(fullName);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 4.22: Gửi email thông báo user vừa bị hạ hạng.
     * Gọi từ TierReevaluateService khi re-evaluate hạ tier.
     */
    public void sendVipDowngraded(String toEmail, String fullName, String reason) {
        String subject = "Thông báo điều chỉnh hạng thành viên - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #555;">THE XUONG - ĐIỀU CHỈNH HẠNG</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Theo chính sách đánh giá định kỳ, tài khoản của anh/chị đã được <strong>điều chỉnh hạ bậc thành viên</strong>.</p>
                    <p><strong>Lý do:</strong> %s</p>
                    <p>Để thăng hạng trở lại, anh/chị chỉ cần tiếp tục mua sắm và tích lũy điểm thưởng theo quy định của các cấp bậc.</p>
                    <p>Hệ thống sẽ tự động nâng hạng ngay khi anh/chị đạt đủ điều kiện (không cần chờ đến kỳ đánh giá).</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/products" style="background-color: #000; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, reason);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 4.23: Gửi email cảnh báo user sắp đến hạn re-evaluate (trong 30 ngày tới).
     * Gọi từ TierWarningJob (cron daily 09:00).
     */
    public void sendVipExpiryWarning(String toEmail, String fullName, java.time.LocalDateTime nextEvaluation) {
        String subject = "Cảnh báo: Hạng thành viên sắp được đánh giá lại - TheXuong";
        String dateStr = nextEvaluation.toLocalDate().toString();
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #d4af37;">THE XUONG - CẢNH BÁO HẠNG THÀNH VIÊN</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Hạng thành viên của anh/chị sẽ được đánh giá lại vào ngày <strong>%s</strong>.</p>
                    <p>Để duy trì hạng hiện tại, anh/chị cần đảm bảo tổng chi tiêu hoặc điểm tích lũy trong 365 ngày qua đạt mức tối thiểu của hạng.</p>
                    <p>Nếu không đạt, hạng sẽ tự động được điều chỉnh giảm vào ngày đánh giá.</p>
                    <p>🎁 Hãy mua sắm thêm hoặc giới thiệu bạn bè để tích điểm và giữ hạng nhé!</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/products" style="background-color: #d4af37; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, dateStr);
        sendHtmlEmail(toEmail, subject, html);
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("[EMAIL] sendHtmlEmail failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // Batch 5: Email notifications for points & vouchers
    // ============================================================

    /**
     * Task 5.7: Gửi email khi user nhận điểm từ đơn hàng.
     * Gọi từ OrderService.confirmReceived sau khi earnPoints.
     */
    public void sendPointsEarned(String toEmail, String fullName, int points, Long orderId, int currentBalance) {
        String subject = "Bạn vừa nhận " + points + " điểm thưởng - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #4CAF50; text-align: center;">THE XUONG - ĐIỂM THƯỞNG</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Bạn vừa nhận được <strong style="color: #4CAF50; font-size: 1.2em;">%d điểm</strong> từ đơn hàng #%d!</p>
                    <p>Tổng số dư hiện tại của bạn: <strong>%d điểm</strong>.</p>
                    <p>Sử dụng điểm để đổi voucher hoặc giảm giá tại checkout.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/loyalty" style="background-color: #4CAF50; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">XEM SỐ DƯ ĐIỂM</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, points, orderId, currentBalance);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 5.8: Gửi email khi user đổi voucher thành công.
     * Gọi từ VoucherService.redeemVoucher sau khi tạo UserVoucher.
     */
    public void sendVoucherRedeemed(String toEmail, String fullName, String voucherCode, String discountAmount, LocalDateTime expiresAt) {
        String subject = "Bạn đã đổi voucher " + discountAmount + " - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #FFC107; text-align: center;">THE XUONG - VOUCHER ĐÃ ĐỔI</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Bạn đã đổi thành công voucher giảm giá!</p>
                    <div style="background: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center;">
                        <p style="margin: 0; font-size: 14px;">Mã voucher của bạn:</p>
                        <p style="margin: 10px 0; font-size: 24px; font-weight: bold; color: #d32f2f; letter-spacing: 2px;">%s</p>
                        <p style="margin: 0; color: #666;">Giảm: %s | Hết hạn: %s</p>
                    </div>
                    <p>Nhập mã này tại checkout để nhận ưu đãi.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/checkout?voucher=%s" style="background-color: #d32f2f; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">ĐẾN CHECKOUT</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, voucherCode, discountAmount, expiresAt.toLocalDate(), voucherCode);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 5.9: Gửi email nhắc nhở voucher sắp hết hạn (trong 3 ngày).
     * Gọi từ VoucherExpiringSoonJob (cron daily 09:00).
     */
    public void sendVoucherExpiring(String toEmail, String fullName, String voucherCode, String discountAmount, LocalDateTime expiresAt, int daysLeft) {
        String subject = "Voucher " + voucherCode + " sắp hết hạn - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #f44336; text-align: center;">THE XUONG - VOUCHER SẮP HẾT HẠN</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Voucher của bạn sẽ hết hạn sau <strong style="color: #f44336;">%d ngày</strong>!</p>
                    <div style="background: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ff9800;">
                        <p style="margin: 0; font-size: 14px;">Mã voucher:</p>
                        <p style="margin: 10px 0; font-size: 24px; font-weight: bold; color: #d32f2f; letter-spacing: 2px;">%s</p>
                        <p style="margin: 0;">Giảm: %s</p>
                        <p style="margin: 5px 0 0 0;">Hết hạn: %s</p>
                    </div>
                    <p>Hãy sử dụng voucher trước khi hết hạn!</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://thexuong.xuansown.id.vn/checkout?voucher=%s" style="background-color: #d32f2f; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">ĐẾN CHECKOUT</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, daysLeft, voucherCode, discountAmount, expiresAt.toLocalDate(), voucherCode);
        sendHtmlEmail(toEmail, subject, html);
    }
}
