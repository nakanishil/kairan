package com.example.kairan.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "districts")
@Data
public class District {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name", length = 255, nullable = false)
	private String name;
	
	@Column(name = "region_code", unique = true, length = 20, nullable = false)
	private String regionCode;
	
	@Column(name = "association", length = 100, nullable = false)
	private String association;
	
	@Column(name = "area", length = 100)
	private String area;
	
	@Column(name = "description", length = 1000)
	private String description;
	
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
