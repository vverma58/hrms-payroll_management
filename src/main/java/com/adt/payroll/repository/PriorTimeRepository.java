package com.adt.payroll.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriorTimeRepository extends JpaRepository<Priortime,Integer>{
	
	 TimeSheetModel findByEmployeeId(int id);
	 
	 Optional<Priortime>   findByEmployeeIdAndDate(int id, String date);

	void save(Optional<Priortime> priortime2);
	List<Priortime> findByEmployeeIdAndStatusIn(int employeeId, List<String> status);


	@Query("SELECT p FROM Priortime p WHERE TO_DATE(p.date, 'DD-MM-YYYY') BETWEEN TO_DATE(:startDate, 'DD-MM-YYYY') AND  TO_DATE(:endDate, 'DD-MM-YYYY')")
	Page<Priortime> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate, Pageable pageable);

}
