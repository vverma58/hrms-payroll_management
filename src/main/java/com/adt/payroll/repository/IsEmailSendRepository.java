package com.adt.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adt.payroll.model.IsEmailSend;

@Repository
public interface IsEmailSendRepository extends JpaRepository<IsEmailSend, Integer> {

}