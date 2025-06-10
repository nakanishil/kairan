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
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "circular_files")
@Data
public class CircularFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "circular_id", nullable = false)
	private Circular circular;
	
	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;
	
	@Column(name = "file_path", nullable = false, length = 512)
	private String filePath;
	
	@Column(name = "uploaded_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Timestamp uploadedAt;
	
	@Column(name = "deleted_at")
	private Timestamp deletedAt;
	
	
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
	
	@PrePersist
	protected void onCreate() {
	    if (uploadedAt == null) {
	        uploadedAt = new Timestamp(System.currentTimeMillis());
	    }
	}
}
