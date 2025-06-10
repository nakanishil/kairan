package com.example.kairan.controller;


import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.example.kairan.service.PaymentService;

@WebMvcTest(PaymentPageController.class)
public class PaymentPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipFeeService membershipFeeService;

    @MockBean
    private PaymentService paymentService;

    private User loginUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() {
        District district = new District();
        district.setId(1);

        Role role = new Role();
        role.setName("ROLE_会員");

        loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserId("testuser");
        loginUser.setPassword("pass");
        loginUser.setEnabled(true);
        loginUser.setRole(role);
        loginUser.setDistrict(district);

        userDetails = new UserDetailsImpl(loginUser, List.of(new SimpleGrantedAuthority("ROLE_会員")));
    }

    @Test
    void paymentStartPage_正常に表示される() throws Exception {
        BigDecimal fee = new BigDecimal("5000");

        when(paymentService.paymentCheck(loginUser)).thenReturn(Optional.of(fee));

        mockMvc.perform(get("/payment/start")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("payment/payment-start"))
            .andExpect(model().attributeExists("fee"))
            .andExpect(model().attribute("fee", fee))
            .andExpect(model().attributeExists("feeDisplay"))
            .andExpect(model().attribute("feeDisplay", "5,000"));
    }
    
    @Test
    void paymentStartPage_金額設定が存在しない場合_エラーメッセージが表示される() throws Exception {
        String errorMessage = "金額設定が存在しません";

        // paymentCheck() が例外を投げるように設定
        when(paymentService.paymentCheck(loginUser))
            .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(get("/payment/start")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("payment/payment-start"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("errorMessage", errorMessage));
    }

}
