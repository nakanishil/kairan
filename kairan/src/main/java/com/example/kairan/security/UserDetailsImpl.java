package com.example.kairan.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.kairan.entity.User;

import lombok.Getter;

@Getter
public class UserDetailsImpl implements UserDetails, org.springframework.security.oauth2.core.user.OAuth2User {
	
	private final User user;
	private final Collection<? extends GrantedAuthority> authorities;
	private final Map<String, Object> attributes;

	
	// 通常ログインコンストラクタ
	public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
		this.attributes = new HashMap<>();
	}
	
	// OAuth2ログインコンストラクタ
	public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
		this.user = user;
		this.authorities = authorities;
		this.attributes = attributes != null ? attributes : new HashMap<>();
	}
	
	// Oauth2Userの実装
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	@Override
	public String getName() {
		return String.valueOf(user.getId());
	}
	
	public User getUser() {
		return user;
	}
	
	// ハッシュ化済みのパスワードを返す
	@Override
	public String getPassword() {
		return user.getPassword();
	}
	
	//ログイン時に利用するユーザIDを返す
	@Override
	public String getUsername() {
		return user.getUserId();
	}
	
	// ロールのコレクションを返す
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	// アカウントが期限切れでなければtrueを返す
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	// ユーザがロックされていなければtrueを返す
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	// ユーザのパスワードが期限切れでなければtrueを返す
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	// ユーザが有効であればtrueを返す
	@Override
	public boolean isEnabled() {
		return user.getEnabled();
	}
}
