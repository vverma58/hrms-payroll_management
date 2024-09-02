package com.adt.payroll.repository;

import com.adt.payroll.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepo extends JpaRepository<Holiday,Integer> {

}
