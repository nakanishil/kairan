package com.example.kairan.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.kairan.entity.Accounting.Type;

import lombok.Data;

@Data
public class AccountingEditForm {
	
	private int id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ←ここ追加！
	@NotNull(message = "取引日を入力してください")
	private LocalDate transactionDate;
	
	@NotNull(message = "収支区分を指定してください")
	private Type type;
	
	@NotNull(message = "カテゴリを選択してください")
	private Integer accountingCategoryId;
	
	@NotNull(message = "金額を入力してください")
    private BigDecimal amount;
	
	@NotBlank(message = "説明文を入力してください")
	@Size(max = 1000, message = "説明文は500文字（全角の場合）以内で入力してください。")
	private String description;
	
	
	

}
