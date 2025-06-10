package com.example.kairan.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ReplyForm {
	@NotBlank(message = "コメントを入力してください。")
	@Size(max = 2000, message = "内容は1,000文字以内(全角の場合)にしてください。")
	private String comment;
}
