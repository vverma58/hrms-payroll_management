package com.adt.payroll.service;

import com.adt.payroll.model.Priortime;

import java.util.List;
import java.util.Optional;

public interface PriorTimeService {
    List<Priortime> getPriorTimeHistoryByEmployeeId(int employeeId);

    List<Priortime> getPriorTimeDetailsByNameAndDate(String fromDate, String toDate);


//    Optional<Priortime> getPriorTimeDetailsByNameAndDate(int employeeId, String name, String fromDate, String toDate);
}

