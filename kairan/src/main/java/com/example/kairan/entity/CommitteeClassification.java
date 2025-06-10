package com.example.kairan.entity;

import java.sql.Timestamp;

import jakarta.persistence.CascadeType;
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
@Table(name = "committee_classification")
@Data
public class CommitteeClassification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne(cascade = CascadeType.MERGE)//District 削除時の対応 
	@JoinColumn(name = "district_id", nullable = false)
	private District district;
	
	@Column(name = "name", nullable = false, length = 50)
	private String name;
	
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
