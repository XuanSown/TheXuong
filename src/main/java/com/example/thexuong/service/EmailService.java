package com.example.thexuong.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
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
                                    <a href="http://localhost:8080/products" style="background-color: #000; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
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
    // Batch 4: Tier email notifications
    // ============================================================

    /**
     * Task 4.21: Gửi email chúc mừng khi user vừa lên hạng VIP lần đầu.
     * Gọi từ OrderService.confirmReceived (sau khi upgrade tier thành công).
     */
    public void sendVipWelcome(String toEmail, String fullName) {
        String subject = "Chúc mừng anh/chị đã lên hạng VIP! - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #d4af37; text-align: center;">THE XUONG - CHÚC MỪNG!</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúc mừng anh/chị đã chính thức trở thành <strong style="color: #d4af37;">Khách hàng VIP</strong> của TheXuong!</p>
                    <h3>Quyền lợi VIP:</h3>
                    <ul>
                        <li>🚚 <strong>Free ship</strong> tất cả các đơn hàng</li>
                        <li>⭐ <strong>+1 điểm bonus</strong> cho mỗi đơn hoàn tất</li>
                        <li>🎁 Voucher VIP riêng (chỉ VIP mới đổi được)</li>
                        <li>🛒 Ưu tiên hỗ trợ đặc biệt</li>
                    </ul>
                    <p>Để giữ hạng VIP, anh/chị cần duy trì tổng chi tiêu 5 triệu đồng HOẶC 50 điểm tích luỹ trong 365 ngày. Hệ thống sẽ tự động re-evaluate vào ngày 1 hàng tháng.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/loyalty" style="background-color: #d4af37; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">XEM ĐIỂM THƯỞNG</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport. All rights reserved.</p>
                </div>
                """.formatted(fullName);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 4.22: Gửi email thông báo user vừa bị hạ từ VIP xuống THUONG.
     * Gọi từ TierReevaluateService khi re-evaluate hạ tier.
     */
    public void sendVipDowngraded(String toEmail, String fullName, String reason) {
        String subject = "Thông báo điều chỉnh hạng thành viên - TheXuong";
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #555;">THE XUONG - HẠ HẠNG</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Theo chính sách re-evaluate hàng tháng, tài khoản của anh/chị đã được chuyển từ <strong>VIP</strong> về <strong>Khách hàng thường</strong>.</p>
                    <p><strong>Lý do:</strong> %s</p>
                    <p>Để lên lại VIP, anh/chị chỉ cần:</p>
                    <ul>
                        <li>Mua hàng với tổng chi tiêu <strong>5 triệu đồng</strong> trong 365 ngày, HOẶC</li>
                        <li>Tích luỹ <strong>50 điểm</strong> từ các đơn hàng hoàn tất</li>
                    </ul>
                    <p>Hệ thống sẽ tự động nâng hạng khi anh/chị đạt ngưỡng (không cần đăng ký).</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/products" style="background-color: #000; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
                    </div>
                    <p style="font-size: 12px; color: #666; text-align: center;">© 2026 The Xuong Sport.</p>
                </div>
                """.formatted(fullName, reason);
        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Task 4.23: Gửi email cảnh báo user VIP sắp đến hạn re-evaluate (trong 30 ngày tới).
     * Gọi từ TierWarningJob (cron daily 09:00).
     */
    public void sendVipExpiryWarning(String toEmail, String fullName, java.time.LocalDateTime nextEvaluation) {
        String subject = "Cảnh báo: Hạng VIP sắp được đánh giá lại - TheXuong";
        String dateStr = nextEvaluation.toLocalDate().toString();
        String html = """
                <div style="font-family: Arial; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0;">
                    <h2 style="color: #d4af37;">THE XUONG - CẢNH BÁO VIP</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Hạng <strong>VIP</strong> của anh/chị sẽ được đánh giá lại vào ngày <strong>%s</strong>.</p>
                    <p>Để giữ hạng VIP, anh/chị cần duy trì trong 365 ngày qua:</p>
                    <ul>
                        <li>Tổng chi tiêu <strong>5 triệu đồng</strong> trở lên, HOẶC</li>
                        <li>Tổng điểm tích luỹ <strong>50 điểm</strong> trở lên</li>
                    </ul>
                    <p>Nếu không đạt, hạng sẽ tự động chuyển về <strong>Khách hàng thường</strong> vào ngày đánh giá.</p>
                    <p>👉 Hãy mua sắm thêm hoặc giới thiệu bạn bè để tích điểm và giữ hạng!</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:8080/products" style="background-color: #d4af37; color: #fff; padding: 10px 20px; text-decoration: none; font-weight: bold;">MUA SẮM NGAY</a>
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
}
