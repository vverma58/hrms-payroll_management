package com.adt.payroll.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.CompOff;

@Repository
public interface CompOffRepository extends JpaRepository<CompOff, Integer> {

	@Query(value = "SELECT * FROM payroll_schema.comp_off c WHERE c.emp_id = ?1 AND c.date = ?2", nativeQuery = true)
	CompOff findCompOffByEmployeeIdAndDate(@Param("emp_id") Integer empId, @Param("date") Date date);

	@Query(value = "SELECT * FROM payroll_schema.comp_off where emp_id=?1 and date=?2 and status='Pending'", nativeQuery = true)
	Optional<CompOff> findByEmpIdAndCompOffDate(Integer empId, Date compOffDate);

	@Query(value = "SELECT * FROM payroll_schema.comp_off where status='Approved'", nativeQuery = true)
	Optional<List<CompOff>> findAllApprovedCompOffData();

}
