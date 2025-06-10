package com.example.kairan.service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// javaMailSenderを使ってメールを送信する
@Service
public class EmailService {
	private final JavaMailSender mailSender;
	
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public void sendEmail(String to, String subject, String content) {
	    try {
	        System.out.println("📨 メール送信開始: " + to);
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(content, true);

	        System.out.println("🔍 メール内容: " + content);
	        System.out.println("📩 宛先: " + to);
	        System.out.println("📜 件名: " + subject);

	        mailSender.send(message);
	        System.out.println("✅ メール送信成功: " + to);
	    } catch(Exception e) {
	        System.out.println("❌ メール送信失敗: " + e.getMessage());
	        e.printStackTrace();
	        throw new RuntimeException("メール送信エラー: " + e.getMessage());
	    }
	}
	
	// パスワードリセット用メール送信
	public void sendIdAndPasswordResetEmail(
			String toEmail, String token)
	{
		try {
			System.out.println("パスワードリセットメール送信開始: " + toEmail);
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			
			String resetUrl = "http://localhost:8080/reset/form?token=" + token;
			
			helper.setTo(toEmail);
			helper.setSubject(" 【Kairan】仮ID・パスワード再発行のお知らせ");
			helper.setText(
					"<p>以下のリンクから仮IDと仮パスワードを発行してください。</p>" +
					"<p><a href=\"" + resetUrl + "\">再発行リンクはこちら</a></p>" + 
					"<p>※このリンクの有効期限は24時間です。</p>",
					true
			);
			
			mailSender.send(message);
			System.out.println("パスワードリセットメール送信成功: " + toEmail);
		} catch(Exception e) {
			System.out.println("パスワードリセットメール送信失敗: " + e.getMessage());
			throw new RuntimeException("パスワードリセットメール送信エラー: " + e.getMessage());
		}
	}

}
