package com.adt.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompOffDTO {

	private String empAdtId;
	private String empName;
	private String empEmailId;
	private String compOffDate;
	private String compOffDay;
	private String compOffStatus;

}