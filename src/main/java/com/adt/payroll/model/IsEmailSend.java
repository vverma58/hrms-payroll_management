package com.adt.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(catalog = "EmployeeDB", schema = "av_schema", name = "is_email_send")
public class IsEmailSend {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "serial")
	private int emailSendId;

	@Column(name = "email_send_status", nullable = false) // bydefault value will be FALSE
	private Boolean emailSendStatus;
}
