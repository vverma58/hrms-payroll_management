package com.adt.payroll.service;

import com.adt.payroll.model.Priortime;

import java.util.List;

public interface PriorTimeService {
    List<Priortime> getPriorTimeHistoryByEmployeeId(int employeeId);
}
