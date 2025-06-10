package com.example.kairan.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class DepositProcessingForm {
	
	private Integer payerId;
	
    @NotNull(message = "年を選択してください。")
    private Integer year; // 年
	
    @NotNull(message = "支払い金額を入力してください。")
	private BigDecimal amount; // 支払金額
    
    @NotNull(message = "取引日を入力してください")
	private LocalDateTime transactionDate; // 取引日
    
    @NotNull(message = "カテゴリを選択してください")
	private Integer paymentMethodId;
	
   	@Size(max = 1000, message = "説明文は500文字（全角の場合）以内で入力してください。")
   	private String Description; // 説明
}
