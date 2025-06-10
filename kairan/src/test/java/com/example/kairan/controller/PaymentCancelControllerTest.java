package com.example.kairan.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentCancelController.class)
public class PaymentCancelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void paymentCancelPage_正常に表示される() throws Exception {
    	mockMvc.perform(get("/payment/cancel")
    	        .with(user("dummyUser").roles("会員")))
    	    .andExpect(status().isOk())
    	    .andExpect(view().name("payment/payment-cancel"));

    }
}
