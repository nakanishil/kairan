package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.District;
import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.NonPaymentService;

@WebMvcTest(NonPaymentController.class)
class NonPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NonPaymentService nonPaymentService;

    @MockBean
    private PaymentMethodRepository paymentMethodRepository;

    private User loginUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // モックユーザー作成
        loginUser = new User();
        District district = new District();
        district.setAssociation("中央町内会");
        district.setArea("1区");
        loginUser.setDistrict(district);
        Role role = new Role();
        role.setName("ROLE_町内会長");
        loginUser.setRole(role);

        userDetails = new UserDetailsImpl(loginUser, List.of(new SimpleGrantedAuthority("ROLE_町内会長")));
    }

    @Test
    void nonPaymentList_正常に表示される() throws Exception {
        int year = Year.now().getValue();
        Page<User> mockPage = new PageImpl<>(List.of());
        List<Integer> yearList = List.of(year, year - 1);

        when(nonPaymentService.nonPaymentPage(any(User.class), eq(year), any(Pageable.class)))
                .thenReturn(mockPage);
        when(nonPaymentService.findMembershipFeeYear("中央町内会")).thenReturn(yearList);

        mockMvc.perform(get("/nonpayment/nonpayment-list")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("nonpayment/nonpayment-list"))
            .andExpect(model().attribute("page", mockPage))
            .andExpect(model().attribute("year", year))
            .andExpect(model().attribute("yearList", yearList));
    }
    
    @Test
    void nonPaymentList_例外が発生した場合はエラーメッセージが表示される() throws Exception {
        int year = Year.now().getValue();

        // ダミーのPage（空）とyearListを返しつつ、errorMessageだけ確認
        Page<User> dummyPage = new PageImpl<>(List.of());
        List<Integer> dummyYearList = List.of();

        // `nonPaymentPage()` は例外をスローする
        when(nonPaymentService.nonPaymentPage(any(User.class), eq(year), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("未対応の役職です"));

        // `findMembershipFeeYear()` が呼ばれたとしても返す
        when(nonPaymentService.findMembershipFeeYear(anyString()))
                .thenReturn(dummyYearList);

        mockMvc.perform(get("/nonpayment/nonpayment-list")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("nonpayment/nonpayment-list"))
            .andExpect(model().attribute("errorMessage", "未対応の役職です"));
    }
    
    @Test
    void depositprocessingPage_正常に表示される() throws Exception {
        int userId = 1;

        // モックデータ
        List<PaymentMethod> mockMethods = List.of(new PaymentMethod());
        List<Integer> mockYearList = List.of(2024, 2023);

        when(paymentMethodRepository.findAll()).thenReturn(mockMethods);
        when(nonPaymentService.findMembershipFeeYear("中央町内会")).thenReturn(mockYearList);

        mockMvc.perform(get("/nonpayment/depositprocessing-page/{id}", userId)
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("nonpayment/depositprocessing-page"))
            .andExpect(model().attributeExists("form"))
            .andExpect(model().attribute("methods", mockMethods))
            .andExpect(model().attribute("yearList", mockYearList));
    }
    
    @Test
    void depositprocessing_正常に処理されリダイレクトされる() throws Exception {
        int payerId = 1;

        // フォームデータ
        String description = "現金支払い";
        String amount = "3000";
        String transactionDate = "2024-04-23T12:00"; // LocalDateTimeにマッピングされる想定
        String paymentMethodId = "1";
        String year = "2024";

        // モック：payer ユーザー
        User payer = new User();
        District district = new District();
        district.setAssociation("中央町内会");
        district.setArea("1区");
        payer.setDistrict(district);

        when(nonPaymentService.findUserId(payerId)).thenReturn(payer);
        doNothing().when(nonPaymentService).nonPaymentRegiCheck(any(), any());
        doNothing().when(nonPaymentService).depositProcessing(any(), any());

        mockMvc.perform(post("/nonpayment/depositprocessing/{id}", payerId)
                .param("description", description)
                .param("amount", amount)
                .param("transactionDate", transactionDate)
                .param("paymentMethodId", paymentMethodId)
                .param("year", year)
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/nonpayment/nonpayment-list"));

        // 呼び出し確認
        verify(nonPaymentService).findUserId(payerId);
        verify(nonPaymentService).nonPaymentRegiCheck(any(), eq(payer));
        verify(nonPaymentService).depositProcessing(eq(payer), any());
    }
    
    @Test
    void depositprocessing_バリデーションエラーがある場合はページを再表示する() throws Exception {
        int payerId = 1;

        // フォームの一部不正（amount未入力）
        String description = "現金支払い";
        String transactionDate = "2024-04-23T12:00";
        String paymentMethodId = "1";
        String year = "2024";

        List<PaymentMethod> mockMethods = List.of(new PaymentMethod());
        List<Integer> mockYearList = List.of(2024, 2023);

        // ページ再描画に必要な依存モック
        when(paymentMethodRepository.findAll()).thenReturn(mockMethods);
        when(nonPaymentService.findMembershipFeeYear("中央町内会")).thenReturn(mockYearList);

        mockMvc.perform(post("/nonpayment/depositprocessing/{id}", payerId)
                .param("description", description)
                // .param("amount", "") ← 入れないことでバリデーションエラーを起こす
                .param("transactionDate", transactionDate)
                .param("paymentMethodId", paymentMethodId)
                .param("year", year)
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("nonpayment/depositprocessing-page"))
            .andExpect(model().attributeExists("form"))
            .andExpect(model().attribute("methods", mockMethods))
            .andExpect(model().attribute("yearList", mockYearList));

        // 決済処理が呼び出されないこと
        verify(nonPaymentService, never()).depositProcessing(any(), any());
    }
    
    @Test
    void depositprocessing_例外が発生した場合はエラーメッセージが表示される() throws Exception {
        int payerId = 1;

        // フォーム入力（正常）
        String description = "現金支払い";
        String amount = "3000";
        String transactionDate = "2024-04-23T12:00";
        String paymentMethodId = "1";
        String year = "2024";

        List<PaymentMethod> mockMethods = List.of(new PaymentMethod());
        List<Integer> mockYearList = List.of(2024, 2023);

        // ページ再描画に必要なモック
        when(paymentMethodRepository.findAll()).thenReturn(mockMethods);
        when(nonPaymentService.findMembershipFeeYear("中央町内会")).thenReturn(mockYearList);

        // 例外をスローさせる（ユーザー取得時）
        when(nonPaymentService.findUserId(payerId)).thenThrow(new IllegalArgumentException("対象ユーザーが見つかりません"));

        mockMvc.perform(post("/nonpayment/depositprocessing/{id}", payerId)
                .param("description", description)
                .param("amount", amount)
                .param("transactionDate", transactionDate)
                .param("paymentMethodId", paymentMethodId)
                .param("year", year)
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("nonpayment/depositprocessing-page"))
            .andExpect(model().attributeExists("form"))
            .andExpect(model().attribute("methods", mockMethods))
            .andExpect(model().attribute("yearList", mockYearList))
            .andExpect(model().attribute("errorMessage", "対象ユーザーが見つかりません"));

        // 決済処理が呼ばれていないことを確認
        verify(nonPaymentService, never()).depositProcessing(any(), any());
    }






}

