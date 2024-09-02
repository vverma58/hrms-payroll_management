package com.adt.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(catalog = "EmployeeDB", schema = "employee_schema", name = "holiday")
@Data
@NoArgsConstructor
@ToString
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int hId;

    @Column(name = "holiday_name")
    private String holidayName;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "month")
    private String month;

    @Column(name = "day")
    private String day;
}
