package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.kairan.service.PasswordResetService;

class PasswordResetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController).build();
    }

    @Test
    void showRequestForm_正常系_リクエスト画面が表示されること() throws Exception {
        mockMvc.perform(get("/reset/request"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset/request-form"))
                .andExpect(model().attributeExists("emailForm"));
    }
    
    @Test
    void requestReset_正常系_リセットメール送信後に成功画面にリダイレクトされること() throws Exception {
        // Arrange
        String email = "test@example.com";


        // Act  Assert
        mockMvc.perform(post("/reset/request")
                .param("email", email)
                .with(csrf())) // CSRF対策
                .andExpect(status().isOk())
                .andExpect(view().name("reset/request-success"))
                .andExpect(model().attributeExists("message"));

        // Serviceメソッドが呼ばれたことも検証
        verify(passwordResetService, times(1)).createPasswordResetToken(email);
    }
    
    @Test
    void requestReset_バリデーションエラー_入力エラーでリクエスト画面に戻ること() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/reset/request")
                .param("email", "") // emailが空
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("reset/request-form"))
                .andExpect(model().attributeHasFieldErrors("emailForm", "email"));

        // Serviceメソッドは呼び出されない
        verify(passwordResetService, never()).createPasswordResetToken(anyString());
    }
    
    @Test
    void requestReset_例外発生_ユーザーが見つからない場合はリクエスト画面に戻ること() throws Exception {
        // Arrange
        String email = "notfound@example.com";

        // createPasswordResetTokenを呼ぶとIllegalArgumentExceptionが発生するように設定
        doThrow(new IllegalArgumentException("該当するユーザーが見つかりません"))
                .when(passwordResetService).createPasswordResetToken(email);

        // Act & Assert
        mockMvc.perform(post("/reset/request")
                .param("email", email)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("reset/request-form"))
                .andExpect(model().attributeHasFieldErrors("emailForm", "email")); // email項目にエラーが付与される
    }
    
    @Test
    void showTemporaryCredentials_正常系_仮IDと仮パスワードが表示されること() throws Exception {
        // Arrange
        String token = "test-token";
        String temporaryUserId = "temp12345678";
        String temporaryPassword = "tempPass1234";

        when(passwordResetService.generateTemporaryCredentials(token))
                .thenReturn(List.of(temporaryUserId, temporaryPassword));

        // Act & Assert
        mockMvc.perform(get("/reset/form")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("reset/temporary-credentials"))
                .andExpect(model().attributeExists("temporaryUserId"))
                .andExpect(model().attributeExists("temporaryPassword"))
                .andExpect(model().attributeExists("message"));
    }
    
    @Test
    void showTemporaryCredentials_異常系_エラーが発生した場合はエラー画面に遷移すること() throws Exception {
        // Arrange
        String token = "invalid-token";

        // generateTemporaryCredentialsを呼ぶと例外をスローするように設定
        when(passwordResetService.generateTemporaryCredentials(token))
                .thenThrow(new IllegalArgumentException("無効なトークンです"));

        // Act & Assert
        mockMvc.perform(get("/reset/form")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("reset/error"))
                .andExpect(model().attributeExists("errorMessage"));
    }






}
