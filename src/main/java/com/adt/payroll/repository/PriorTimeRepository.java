package com.adt.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;

public interface PriorTimeRepository extends JpaRepository<Priortime,Integer>{
	
	 TimeSheetModel findByEmployeeId(int id);
	 
	 Optional<Priortime>   findByEmployeeIdAndDate(int id, String date);

	void save(Optional<Priortime> priortime2);
	List<Priortime> findByEmployeeIdAndStatusIn(int employeeId, List<String> status);
	
	// Priortime findByPrior(int id, String date);


}
