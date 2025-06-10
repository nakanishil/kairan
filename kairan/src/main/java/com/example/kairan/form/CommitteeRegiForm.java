package com.example.kairan.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CommitteeRegiForm {
	@NotBlank(message = "委員名を入力してください。")
	@Size(max = 50, message = "委員名は25文字以内(全角の場合)で入力してください。")
	private String name;
}	
