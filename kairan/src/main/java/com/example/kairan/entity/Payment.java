package com.example.kairan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "payments")
public class Payment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false)
	private BigDecimal amount;
	
	@ManyToOne
	@JoinColumn(name = "membership_fee_id")
	private MembershipFee membershipFee;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_method_id", nullable = false)
	private PaymentMethod paymentMethod;
	
	@Column(nullable = false)
	private String status;
	
	@Column(name = "transaction_id", nullable = false)
	private String transactionId;
	
	@Column(name = "payment_date", nullable = false)
	private LocalDateTime paymentDate;
	
	@Column(name = "due_date", nullable = false)
	private LocalDateTime dueDate;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "deletedAt")
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

}
