package com.example.thexuong.security;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.LoginHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    @Test
    void lockedUser_redirectsToLoginWithLockedFlagAndNoTokens() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService cookieService = mock(JwtCookieService.class);
        LoginHistoryService loginHistoryService = mock(LoginHistoryService.class);

        OAuth2SuccessHandler handler =
                new OAuth2SuccessHandler(userRepository, jwtService, cookieService, loginHistoryService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:5173");

        User locked = User.builder()
                .email("locked@test.com")
                .username("locked@test.com")
                .fullName("Locked User")
                .provider("GOOGLE")
                .role("CUSTOMER")
                .active(false)
                .build();
        when(userRepository.findByEmail("locked@test.com")).thenReturn(Optional.of(locked));

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(), Map.of("email", "locked@test.com", "name", "Locked User"), "email");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login?locked=1");
        verify(jwtService, never()).generateAccessToken(any());
        verify(loginHistoryService).recordLogin(
                eq("locked@test.com"), anyString(), any(), eq("GOOGLE"), eq(false), anyString());
    }
}
