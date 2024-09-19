package com.adt.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.ResponseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.ResponseModel;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;

public interface TimeSheetService {

	public String updateCheckIn(int empId, double latitude, double longitude) throws ParseException;

	public String updateCheckOut(int empId, double latitude, double longitude) throws ParseException;

	CheckStatusDTO checkStatus(int empId);

	List<ResponseModel> checkPriorStatus(int empId);

//-------------------------------------------------------------------------------------------------------------

	List<TimesheetDTO> empAttendence(int empId, LocalDate fromDate, LocalDate toDate);

	List<TimeSheetModel> allEmpAttendence(LocalDate fromDate, LocalDate toDate);

	Optional<Priortime> savePriorTime(PriorTimeManagementRequest priorTimeManagementRequest, double latitude,
			double longitude) throws ParseException;

	TimeSheetModel saveConfirmedDetails(Optional<Priortime> priortime) throws ParseException;

	public String pauseWorkingTime(int empId);

	public String resumeWorkingTime(int empId) throws ParseException;

	public EmployeeExpenseDTO employeeExpense(int empId, List<MultipartFile> image, EmployeeExpense employeeExpense)
			throws IOException;

	public EmployeeExpenseDTO acceptedEmployeeExpense(int expenseId, EmployeeExpenseDTO employeeExpenseDTO)
			throws IOException;

	// public EmployeeExpenseDTO rejectedEmployeeExpense(int expenseId);
	// public EmployeeExpenseDTO getEmployeeExpenseById(int expenseId);
	// public String getEmployeeExpenseById(int expenseId);

	public String approveEmployeeExpenseById(int expenseId);

	public String rejectEmployeeExpenseById(int expenseId);

	List<EmployeeExpense> getAllExpenseDetail();

	public ByteArrayInputStream getExcelData(LocalDate fromDate, LocalDate toDate) throws IOException;

	public String reSendPriorTimeRequest(int priortimeId);

	public String checkInCheckOutForContractBasedEmployee(String hours, String date, double latitude, double longitude,
			int empId);

	public String earlyCheckOut(double latitude, double longitude, int empId, String reason, String reasonType)
			throws ParseException;

	String updateCheckInCheckOutByEmpId(int empId, String checkInTime, String checkOutTime, String date);

	Optional<TimeSheetModel> getTimeSheetByEmployeeIdAndDate(int employeeId, String date);

	Page<Map.Entry<Integer, List<Priortime>>> getAllEmployeePriorTimeRequest(int page, int size);

// running code(gateway+postman) but URL not build on click button(mail), so will check after completing other task----
//	public String empCompOffApprovedOrRejected(Integer empId, String compOffDate, String compOffStatus)
//			throws TemplateException, MessagingException, IOException, ParseException;

	public ResponseDTO getAllApprovedCompOffData();

}
