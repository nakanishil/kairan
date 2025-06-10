package com.example.kairan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Data
@Table(name = "accounting")
public class Accounting {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	// 紐づく町内会
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "district_id", nullable = false)
	private District district;
	
	// 登録者
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recorded_by", nullable = false)
	private User recordedBy;
	
	// 収入 or 支出
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type;
	
	// 会計カテゴリ
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_category_id", nullable = false)
	private AccountingCategory accountingCategory;
	
	// 金額
	@Column(nullable = false)
	private BigDecimal amount;
	
	// 説明
	@Column(columnDefinition = "TEXT")
	private String description;
	
	// 取引日
	@Column(name = "transaction_date", nullable = false)
	private LocalDateTime transactionDate;
	
	// 作成日
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	// 更新日
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	// 削除日(ソフトデリート用）
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
    // 登録前に作成日セット
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // 更新前に更新日セット
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ソフトデリートメソッド
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // ENUM Type（収入 or 支出）
    public enum Type {
        収入, 支出
    }
	
}
