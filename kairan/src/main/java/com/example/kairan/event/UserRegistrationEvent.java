package com.example.kairan.event;

import org.springframework.context.ApplicationEvent;

import com.example.kairan.entity.User;

// ユーザ登録後にUserRegistrationEventを発行し、メール送信をトリガーする
public class UserRegistrationEvent extends ApplicationEvent{
	private final User user;
	private final String appUrl;
	
	// 250605 JunitテストのためにObject sourceを追加
	public UserRegistrationEvent(Object source, User user,String appUrl) {
		super(user);
		this.user = user;
		this.appUrl = appUrl;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getAppUrl() {
		return appUrl;
	}
}
