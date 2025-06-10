package com.example.kairan.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
	
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static void main(String[] args) {
        String rawPassword = "hashedpassword";
        String hashedPassword = hash(rawPassword);
        System.out.println("Hashed Password:" + hashedPassword);
    }
//	public static void main(String[] args) {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		String rawPassword = "hashedpassword"; // ここにハッシュ化したいパスワードを入力
//		String hashedPassword = encoder.encode(rawPassword);
//		System.out.println("Hashed Password:" + hashedPassword);
//	}
}
