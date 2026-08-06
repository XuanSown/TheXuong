package com.example.thexuong.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // leniency for when createMimeMessage is called
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_Success() throws MessagingException {
        emailService.sendEmail("test@example.com");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_MessagingException_CatchesAndContinues() throws Exception {
        // MimeMessageHelper.setSubject calls MimeMessage.setSubject(String, String)
        doThrow(new MessagingException("Mocked error")).when(mimeMessage).setSubject(anyString(), anyString());

        assertDoesNotThrow(() -> emailService.sendEmail("test@example.com"));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendNewPassword_Success() {
        emailService.sendNewPassword("test@example.com", "newPass123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("test@example.com", message.getTo()[0]);
        assertEquals("TheXuong", message.getFrom());
        assertEquals("Cấp lại mật khẩu mới - TheXuong", message.getSubject());
        assertTrue(message.getText().contains("newPass123"));
    }

    @Test
    void sendNewPassword_MailException_Propagates() {
        doThrow(new org.springframework.mail.MailSendException("Error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(org.springframework.mail.MailSendException.class, () -> 
            emailService.sendNewPassword("test@example.com", "newPass123"));
    }

    @Test
    void sendPasswordResetLink_Success() throws Exception {
        emailService.sendPasswordResetLink("test@example.com", "http://reset.link");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetLink_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendPasswordResetLink("test@example.com", "http://reset.link"));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendPasswordChangedConfirmation_Success() throws Exception {
        emailService.sendPasswordChangedConfirmation("test@example.com");
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    void sendPasswordChangedConfirmation_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendPasswordChangedConfirmation("test@example.com"));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendVipWelcome_Success() throws Exception {
        emailService.sendVipWelcome("test@example.com", "Nguyen Van A");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVipWelcome_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendVipWelcome("test@example.com", "Nguyen Van A"));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendVipDowngraded_Success() throws Exception {
        emailService.sendVipDowngraded("test@example.com", "Nguyen Van A", "Khong du chi tieu");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVipDowngraded_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendVipDowngraded("test@example.com", "Nguyen Van A", "Khong du chi tieu"));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendVipExpiryWarning_Success() throws Exception {
        emailService.sendVipExpiryWarning("test@example.com", "Nguyen Van A", LocalDateTime.now());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVipExpiryWarning_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendVipExpiryWarning("test@example.com", "Nguyen Van A", LocalDateTime.now()));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendPointsEarned_Success() throws Exception {
        emailService.sendPointsEarned("test@example.com", "Nguyen Van A", 50, 1001L, 200);
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    void sendPointsEarned_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendPointsEarned("test@example.com", "Nguyen Van A", 50, 1001L, 200));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendVoucherRedeemed_Success() throws Exception {
        emailService.sendVoucherRedeemed("test@example.com", "Nguyen Van A", "TX-12345", "50.000đ", LocalDateTime.now());
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    void sendVoucherRedeemed_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendVoucherRedeemed("test@example.com", "Nguyen Van A", "TX-12345", "50.000đ", LocalDateTime.now()));
        verify(mailSender, never()).send(mimeMessage);
    }

    @Test
    void sendVoucherExpiring_Success() throws Exception {
        emailService.sendVoucherExpiring("test@example.com", "Nguyen Van A", "TX-54321", "20.000đ", LocalDateTime.now(), 3);
        verify(mailSender).send(mimeMessage);
    }
    
    @Test
    void sendVoucherExpiring_Exception_Catches() throws Exception {
        doThrow(new MessagingException("Mock error")).when(mimeMessage).setSubject(anyString(), anyString());
        assertDoesNotThrow(() -> emailService.sendVoucherExpiring("test@example.com", "Nguyen Van A", "TX-54321", "20.000đ", LocalDateTime.now(), 3));
        verify(mailSender, never()).send(mimeMessage);
    }
}
