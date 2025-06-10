package com.example.kairan.form;

import java.text.Normalizer;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserRegiForm {

    @NotBlank(message = "メールアドレスの入力をしてください。")
    @Size(max = 60, message = "メールアドレスは60文字以内にしてください。")
    @Email(message = "メールアドレスは正しい形式で入力してください。")
    private String email;
    
    @NotBlank(message = "希望するIDの入力をしてください。")
    @Size(max = 32, message = "IDは32文字以内にしてください。")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "IDは半角英数字とハイフン(-)、アンダースコア(_) のみ使用できます。")
    private String userId;
    
    @NotBlank(message = "パスワードの入力をしてください。")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内にしてください。")
    private String password;
    
    @NotBlank(message = "確認用パスワードの入力をしてください。")
    @Size(min = 8, max = 100, message = "確認用パスワードは8文字以上100文字以内にしてください。")
    private String confirmPassword;
    
    @NotBlank(message = "氏名の入力をしてください。")
    @Size(max = 70, message = "氏名は70文字以内（全角）にしてください。")
    private String name;
    
    @NotBlank(message = "フリガナの入力をしてください。")
    @Size(max = 100, message = "フリガナは100文字以内（全角）にしてください。")
    @Pattern(regexp = "^[ァ-ヶ\u30FC]+$", message = "フリガナは全角カタカナのみ使用できます。")
    private String furigana;
    
    @NotBlank(message = "電話番号の入力をしてください。")
    @Size(max = 20, message = "電話番号は20文字以内にしてください。")
    @Pattern(regexp = "^\\d{10,11}$", message = "電話番号はハイフンなしの半角数字のみ入力してください。")
    private String phoneNumber;
    
    @NotBlank(message = "郵便番号の入力をしてください。")
    @Pattern(regexp = "^\\d{7}$", message = "郵便番号はハイフンなしの7桁の数字を入力してください。")
    private String postalCode;
    
    @NotBlank(message = "住所の入力をしてください。")
    @Size(max = 255, message = "住所は255文字以内にしてください。")
    @Pattern(regexp = "^[ぁ-んァ-ヶ一-龠0-9\u30FC-]+$", message = "住所は全角日本語・半角数字・半角ハイフンのみ使用できます。")
    private String address;
    
    // 入力データの整形
    public void normalize() {
        if (this.name != null) {
            this.name = this.name.replace(" ", "").replace("　", ""); // 氏名の半角・全角スペース削除
        }

        if (this.furigana != null) {
            this.furigana = Normalizer.normalize(this.furigana, Normalizer.Form.NFKC)
                    .replace(" ", "").replace("　", ""); // 半角カナ→全角カナ + スペース削除
        }
    }
    
    // IDとパスワードの同一禁止
    @AssertTrue(message = "IDとパスワードは異なるものを入力してください。")
    public boolean isPasswordDifferentFromUserId() {
        return password != null && userId != null && !password.equals(userId);
    }
}
