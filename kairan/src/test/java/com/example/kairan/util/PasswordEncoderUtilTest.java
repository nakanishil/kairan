package com.example.kairan.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PasswordEncoderUtilTest {
	
	@Test
	void main_パスワードが正常にハッシュ化される() throws Exception{
		String rawPassword = "test123";
		String hashed = PasswordEncoderUtil.hash(rawPassword);
		
		assertNotNull(hashed); //nullじゃないこと
		assertNotEquals(rawPassword, hashed); // 生パスワードと違うこと
		
		// ハッシュが正しく検証されるか
		assertTrue(new org.springframework.security.crypto.bcrypt
				.BCryptPasswordEncoder().matches(rawPassword, hashed));
		
	}
	
	
}
