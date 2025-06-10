package com.example.kairan.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CircularForm {
	
	@NotBlank(message = "回覧板の名前を入力してください。")
	@Size(max = 120, message = "回覧板の名前は60文字以内(全角の場合)にしてください。")
	private String name;
	
	@Size(max = 250, message = "説明は125文字以内(全角の場合)にしてください。")
	private String description;
	
	// 緊急性
	private Boolean isUrgent;
	
	// ファイルごとの表示名と実ファイルを含むフォームのリスト
	private List<CircularFileForm> fileList;
}
