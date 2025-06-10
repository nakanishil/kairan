package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Year;
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
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.example.kairan.service.PaymentService;

@WebMvcTest(PaymentSuccessController.class)
public class PaymentSuccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipFeeService membershipFeeService;

    @MockBean
    private PaymentService paymentService;

    private User loginUser;
    private UserDetailsImpl userDetails;
    private MembershipFee mockFee;

    @BeforeEach
    void setup() {
        District district = new District();
        district.setId(1);

        Role role = new Role();
        role.setName("ROLE_会員");

        loginUser = new User();
        loginUser.setId(123);
        loginUser.setDistrict(district);
        loginUser.setRole(role);
        loginUser.setEnabled(true);
        loginUser.setUserId("testuser");
        loginUser.setPassword("pass");

        userDetails = new UserDetailsImpl(loginUser, List.of(new SimpleGrantedAuthority("ROLE_会員")));

        mockFee = new MembershipFee();
        mockFee.setAmount(new BigDecimal("5000"));

        when(membershipFeeService.sameDistrictIdAndYear(eq(1), eq(Year.now().getValue())))
            .thenReturn(Optional.of(mockFee));
    }
    
    @Test
    void paymentSuccess_正常に処理される() throws Exception {
    	String sessionId = "test-session-123";
    	
    	mockMvc.perform(get("/payment/success")
    			.param("session_id", sessionId)
    			.with(user(userDetails)))
    			.andExpect(status().isOk())
    			.andExpect(view().name("payment/payment-success"))
    			.andExpect(model().attribute("sessionId",sessionId));
    	
    	verify(paymentService).regiPaymentAndAccounting(eq(loginUser), eq(mockFee), eq(sessionId));
    			
    }
}
