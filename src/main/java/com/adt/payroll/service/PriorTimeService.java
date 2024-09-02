package com.adt.payroll.service;

import com.adt.payroll.model.Priortime;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PriorTimeService {
    List<Priortime> getPriorTimeHistoryByEmployeeId(int employeeId);

    Page<Priortime> getPriorTimeDetailsByDateRange(String fromDate, String toDate,int page,int size);

}

