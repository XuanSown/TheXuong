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
}
