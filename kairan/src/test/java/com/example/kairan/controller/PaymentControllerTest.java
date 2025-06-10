package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipFeeService membershipFeeService;

    private User mockUser;
    private MembershipFee mockFee;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() {
        District mockDistrict = new District();
        mockDistrict.setId(1);

        mockUser = new User();
        mockUser.setId(123);
        mockUser.setDistrict(mockDistrict);
        mockUser.setUserId("testuser");
        mockUser.setPassword("testpass");
        mockUser.setEmail("test@example.com");
        mockUser.setEnabled(true);

        Role role = new Role();
        role.setName("ROLE_会員");
        mockUser.setRole(role);

        mockFee = new MembershipFee();
        mockFee.setAmount(new BigDecimal("5000"));

        userDetails = new UserDetailsImpl(
            mockUser,
            List.of(new SimpleGrantedAuthority("ROLE_会員"))
        );
    }


    @Test
    void createCheckoutSession_正常にリダイレクトされる() throws Exception {
        when(membershipFeeService.sameDistrictIdAndYear(eq(1), eq(Year.now().getValue())))
            .thenReturn(Optional.of(mockFee));

        Session mockedSession = mock(Session.class);
        when(mockedSession.getUrl()).thenReturn("http://mock-stripe.com/success");

        try (MockedStatic<Session> mockedStatic = Mockito.mockStatic(Session.class)) {
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                        .thenReturn(mockedSession);

            mockMvc.perform(MockMvcRequestBuilders.post("/payment/create-checkout-session")
                    .with(user(userDetails)) 
            		.with(csrf())) 
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://mock-stripe.com/success"));
        }
    }
}
	