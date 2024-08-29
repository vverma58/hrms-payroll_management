package com.adt.payroll.model;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(catalog = "EmployeeDB", schema = "payroll_schema", name = "comp_off")
@Data
public class CompOff {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Integer id;

//    @ManyToOne
//	@JoinColumn(name = "emp_id", referencedColumnName = "EMPLOYEE_ID", nullable = true, insertable = false, updatable = false)
//	private User employee;
	
//	@Column(name = "emp_id")
//    private int empId;
	@ManyToOne
	@JoinColumn(name = "empId", referencedColumnName = "EMPLOYEE_ID", nullable = false, insertable = false, updatable = false)
	private User employee;
	private int empId;
	
    @Column(name = "checkout")
    private Time checkout;

    @Column(name = "checkin")
    private Time checkin;

    @Column(name = "date")
    private Date date;

    @Column(name = "status")
    private String status;

    @Column(name = "expiry_time")
    private Timestamp expiryTime;

    public CompOff() {
        super();
    }

	public CompOff(Integer id, User employee, int empId, Time checkout, Time checkin, Date date, String status,
			Timestamp expiryTime) {
		super();
		this.id = id;
		this.employee = employee;
		this.empId = empId;
		this.checkout = checkout;
		this.checkin = checkin;
		this.date = date;
		this.status = status;
		this.expiryTime = expiryTime;
	}

	@Override
	public String toString() {
		return "CompOff [id=" + id + ", employee=" + employee + ", empId=" + empId + ", checkout=" + checkout
				+ ", checkin=" + checkin + ", date=" + date + ", status=" + status + ", expiryTime=" + expiryTime + "]";
	}

	

	
	
   
}
