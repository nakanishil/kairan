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
@Table(name = "messages")
@Data
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "board_type_id", nullable = false)
	private BoardType boardType;
	
	@ManyToOne
	@JoinColumn(name = "committee_type")
	private CommitteeClassification committeeType;
	
	@Column(name = "title", length = 100, nullable = true)
	private String title;
	
	@Column(name = "comment", nullable = false)
	private String comment;
	
	@Column(name = "parent_id")
	private Integer parentId;
	
	@Column(name = "status", length = 50)
	private String status;
	
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
	public void onUpdate() {
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
