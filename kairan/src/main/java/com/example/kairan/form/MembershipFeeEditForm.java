package com.example.kairan.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MembershipFeeEditForm {
	
	private Integer id;

	// 年度
	@NotNull(message = "年度を選択してください。")
	private Integer year;

	// 金額
	@NotNull(message = "金額を入力してください。")
	@Min(value = 0, message = "金額は0円以上にしてください。")
	private Integer amount;

}
