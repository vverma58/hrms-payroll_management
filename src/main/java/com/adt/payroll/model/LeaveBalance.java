package com.adt.payroll.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "leave_balance")
public class LeaveBalance {

	@Id
	@Column(name = "leave_bal_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leave_balance_seq")
	@SequenceGenerator(name = "leave_balance_seq", allocationSize = 1, schema = "payroll_schema")
	private int leaveBalanceId;

	@Column(name = "name")
	private String name;

	@Column(name = "leave_balance")
	private int leaveBalance;

	@OneToOne
	@JoinColumn(name = "emp_id", referencedColumnName = "EMPLOYEE_ID", nullable = true, insertable = false, updatable = false)
	private User employee;
	private Integer emp_id;

	@Column(name = "updated_when")
	private Timestamp updatedWhen;

}
