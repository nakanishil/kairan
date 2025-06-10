package com.example.kairan.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AdminDistrictRegiForm {

	@NotBlank(message = "町名を入力してください。")
	@Size(max = 255, message = "町名は120文字(全角)以内に入力してください。")
	private String name;

	@NotBlank(message = "地方公共団体コードを入力してください。")
	@Pattern(regexp = "\\d{6}", message = "地方公共団体コードは6桁の数字で入力してください。")
	private String regioncode;

	@NotBlank(message = "町内会名を入力してください。") 
	@Size(max = 100, message ="町内会名は50文字以内で入力してください。")
	private String association;

	@NotBlank(message = "町内会の区を入力してください。")
	@Size(max = 100, message = "町内会の区は50文字(全角)で入力してください。")
	private String area;

	@Size(max = 1000, message = "説明文は500文字（全角の場合）以内で入力してください。")
	private String description;

	// 入力データの整形
	public void normalize() {
		if(this.name != null) {
			this.name = this.name.replace(" ", "").replace("　", ""); //全角、半角スペースの削除
		}
		if(this.area != null) {
			this.area = this.area.replace(" ", "").replace("　", ""); //全角、半角スペースの削除
		}

	}
}
