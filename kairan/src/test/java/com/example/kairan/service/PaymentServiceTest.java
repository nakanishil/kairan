package com.example.kairan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Payment;
import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.User;
import com.example.kairan.repository.AccountingCategoryRepository;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class) 
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private AccountingRepository accountingRepository;

    @Mock
    private AccountingCategoryRepository accountingCategoryRepository;

    private User mockUser;
    private MembershipFee mockFee;
    private AccountingCategory mockCategory;

    @BeforeEach
    public void setup() {

        mockUser = new User();
        mockUser.setId(1);
        District district = new District();
        district.setId(10);
        mockUser.setDistrict(district);

        mockFee = new MembershipFee();
        mockFee.setAmount(new BigDecimal("5000"));

        mockCategory = new AccountingCategory();
        mockCategory.setId(1);
        
        PaymentMethod mockMethod = new PaymentMethod();
        mockMethod.setId(1);
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(mockMethod));


        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.of(mockCategory));
    }

    @Test
    void regiPaymentAndAccounting_正常に保存される() {
        String sessionId = "test-session-123";

        paymentService.regiPaymentAndAccounting(mockUser, mockFee, sessionId);

        // 保存されたPaymentの内容確認
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        
        assertEquals(mockUser, savedPayment.getUser());
        assertEquals(mockFee.getAmount(), savedPayment.getAmount());
        assertEquals("PAID", savedPayment.getStatus());
        assertEquals(sessionId, savedPayment.getTransactionId());
        assertTrue(savedPayment.getPaymentDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        
        // 保存されたAccountingの内容確認
        ArgumentCaptor<Accounting> accountingCaptor = ArgumentCaptor.forClass(Accounting.class);
        verify(accountingRepository).save(accountingCaptor.capture());
        Accounting savedAccounting = accountingCaptor.getValue();
        
        assertEquals(mockUser.getDistrict(), savedAccounting.getDistrict());
        assertEquals(mockUser, savedAccounting.getRecordedBy());
        assertEquals(Accounting.Type.収入, savedAccounting.getType());
        assertEquals(mockFee.getAmount(), savedAccounting.getAmount());
        assertEquals("町内会費支払い(stripe)", savedAccounting.getDescription());
        assertEquals(savedPayment.getPaymentDate(), savedAccounting.getTransactionDate());
        
    }
    
    @Test
    void regiPaymentAndAccounting_カテゴリが存在しない場合_例外がスローされる() {
        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            paymentService.regiPaymentAndAccounting(mockUser, mockFee, "session-test-123");
        });
    }
}
