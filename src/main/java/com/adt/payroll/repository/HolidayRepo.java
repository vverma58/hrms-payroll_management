package com.adt.payroll.repository;

import com.adt.payroll.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HolidayRepo extends JpaRepository<Holiday,Integer> {

    @Query(value = "SELECT * FROM employee_schema.holiday WHERE month = ?1", nativeQuery = true)
    List<Holiday> findHolidaysByMonth(String month);


}
