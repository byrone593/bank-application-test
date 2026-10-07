package com.devsu.hackerearth.backend.account.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account extends Base {
    @Column(name = "account_number", unique = true, nullable = false)
    private String number;
	private String type;
	@Column(nullable = false, precision = 19, scale = 2)
    private double initialAmount;
	private boolean isActive;
    @Column(name = "client_id")
    private Long clientId;
}
