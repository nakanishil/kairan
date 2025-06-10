package com.example.kairan.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
					.requestMatchers("/css/**", "/images/**", "/strage/**", "/signup/**", "/login", "/js/**",
							"/fragments/**", "/layout/**","/reset/**").permitAll() // 全ユーザアクセス許可
					.requestMatchers("/admin/**").hasRole("行政") // 区長のみアクセス許可するURL
					.requestMatchers("/user/member-list", "/committee/list", "/accounting/**",
							"membership-fee/**", "/circular/control/**")
								.hasRole("町内会長") // 町内会長のみアクセス許可するURL
					.requestMatchers("/user/area-members").hasRole("区長") // 区長のみアクセス許可するURL
					.requestMatchers("/user/committee-members").hasAnyRole("委員長","委員") // 委員長のみアクセス許可するURL
					.requestMatchers("/nonpayment/**").hasAnyRole("町内会長", "区長")
					.anyRequest().authenticated() // 上記以外のURLはログインが必要
			)
			.formLogin((form) -> form
					.loginPage("/login") // ログインページURL
					.loginProcessingUrl("/login") // ログインフォームの送信先URL
					.defaultSuccessUrl("/", true) // ログイン成功時のリダイレクト先URL
					.failureUrl("/login?error") // ログイン失敗時のリダイレクト先URL
					.permitAll()
			)
			.oauth2Login(oauth -> oauth
					.loginPage("/login")
					.defaultSuccessUrl("/", true)
			)
			.logout((logout) -> logout
					.logoutUrl("/logout")
					.logoutSuccessUrl("/login") // ログアウト成功後の遷移先
					.invalidateHttpSession(true) // セッションを無効化
					.deleteCookies("JSESSIONID")
					.permitAll()
			);
		
		return http.build();
	}
	
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
