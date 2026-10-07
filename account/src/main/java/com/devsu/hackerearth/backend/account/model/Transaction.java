package com.devsu.hackerearth.backend.account.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends Base {
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transaction_date", nullable = false)
	private Date date;
	private String type;
	@Column(nullable = false, precision = 19, scale = 2)
	private double amount;
	@Column(nullable = false, precision = 19, scale = 2)
	private double balance;

	@Column(name = "account_id")
	private Long accountId;
}
