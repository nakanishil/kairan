package com.example.kairan.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "payment_methods")
public class PaymentMethod {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
