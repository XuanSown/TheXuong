package com.example.thexuong.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleDisabledAccount_returns423WithLockedMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handleDisabledAccount(new DisabledException("User is disabled"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
    }
}
