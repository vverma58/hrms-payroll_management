package com.adt.payroll.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
	
	// Priortime findByPrior(int id, String date);

	@Query("SELECT t FROM Priortime t WHERE t.date BETWEEN :fromDate AND :toDate")
	List<Priortime> findByEmployeeIdAndNameAndDateRange(
			@Param("fromDate") String fromDate,
			@Param("toDate") String toDate);

//
//	@Query("SELECT t FROM Priortime t WHERE t.employeeId = :employeeId AND t.name = :name AND t.date BETWEEN :fromDate AND :toDate")
//	Optional<Priortime> findByEmployeeIdAndNameAndDateRange(
//			@Param("employeeId") int employeeId,
//			//@Param("name") String name,
//			@Param("fromDate") LocalDate fromDate,
//			@Param("toDate") LocalDate toDate);
}
