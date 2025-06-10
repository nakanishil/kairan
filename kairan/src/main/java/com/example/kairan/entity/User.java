package com.example.kairan.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "email", unique = true, nullable = false, length = 60)
	private String email;
	
	@Column(name = "user_id", unique = true, nullable = false, length = 32)
	private String userId;
	
	@Column(name = "password", nullable = false, length = 255)
	private String password;
	
	@Column(name = "name", nullable = false, length = 70)
	private String name;
	
	@Column(name = "furigana", nullable = false, length = 100)
	private String furigana;
	
	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;
	
	@Column(name = "postal_code", nullable = false, length = 10)
	private String postalCode;
	
	@Column(name = "address", nullable = false, length = 255)
	private String address;
	
	@ManyToOne
	@JoinColumn(name = "district_id", nullable = false)
	private District district;
	
	@Column(name = "enabled", nullable = false)
	private Boolean enabled = false;
	
	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;
	
	@ManyToOne
	@JoinColumn(name = "committee_id")
	private CommitteeClassification committee;
	
	@Column(name = "google_id")
	private String googleId;
	
	@Column(name = "google_linked")
	private boolean googleLinked;
	
	@Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Timestamp createdAt;
	
	@Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private Timestamp updatedAt;
	
	@Column(name = "deleted_at")
	private Timestamp deletedAt;
	
	
	// 登録時に作成日をセット
	@PrePersist
	protected void onCreate() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		createdAt = now;
		updatedAt = now;
	}
	
	// 更新時に更新日をセット
	@PreUpdate
	protected void onUpdate() {
		updatedAt = new Timestamp(System.currentTimeMillis());
	}
	
	// ソフトデリート
	public void softDelete() {
		deletedAt = new Timestamp(System.currentTimeMillis());
	}
	
	// ソフトデリート済みかどうかのチェック
	public boolean isDeleted() {
		return deletedAt != null;
	}
	
	// ソフトデリートからの復帰
	public void restore() {
		deletedAt = null;
	}
}
