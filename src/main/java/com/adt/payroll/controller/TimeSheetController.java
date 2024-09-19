package com.adt.payroll.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.config.Auth;
import com.adt.payroll.dto.CheckStatusDTO;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.dto.ResponseDTO;
import com.adt.payroll.dto.TimesheetDTO;
import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.exception.PriorTimeAdjustmentException;
import com.adt.payroll.model.CompOff;
import com.adt.payroll.model.EmployeeExpense;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.model.payload.ApiResponse;
import com.adt.payroll.model.payload.PriorTimeManagementRequest;
import com.adt.payroll.msg.HttpStatusMapper;
import com.adt.payroll.msg.ResponseModel;
import com.adt.payroll.repository.CompOffRepository;
import com.adt.payroll.repository.PriorTimeRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;
import com.adt.payroll.service.PriorTimeService;
import com.adt.payroll.service.TimeSheetService;
import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/timeSheet")
public class TimeSheetController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CompOffRepository compOffRepo;

	@Autowired
	private TimeSheetService timeSheetService;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private PriorTimeService priortimeService;

	@Autowired
	private PriorTimeRepository priorTimeRepository;

	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Autowired
	private Configuration freemarkerConfig;

	@Value("${-Dmy.port}")
	private String serverPort;

	@Value("${-Dmy.property}")
	private String ipaddress;

	@Value("${-UI.scheme}")
	private String scheme;

	@Value("${-UI.context}")
	private String context;

	@PreAuthorize("@auth.allow('CHECK_IN',T(java.util.Map).of('currentUser', #empId))")
	@PostMapping("/checkIn/{empId}")
	public ResponseEntity<String> saveCheckIn(@RequestParam("Latitude") double latitude,
			@RequestParam("Longitude") double longitude, @PathVariable int empId, HttpServletRequest request)
			throws ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(timeSheetService.updateCheckIn(empId, latitude, longitude));
	}

	@PreAuthorize("@auth.allow('CHECK_OUT',T(java.util.Map).of('currentUser', #empId))")
	@PutMapping("/checkOut/{empId}")
	public ResponseEntity<String> saveCheckOut(@RequestParam("Latitude") double latitude,
			@RequestParam("Longitude") double longitude, @PathVariable int empId, HttpServletRequest request)
			throws ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.updateCheckOut(empId, latitude, longitude), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('CHECK_STATUS_FROM_TIMESHEET',T(java.util.Map).of('currentUser', #empId))")
	@PostMapping("/checkStatus/{empId}")
	public ResponseEntity<CheckStatusDTO> checkStatus(@PathVariable int empId, HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.checkStatus(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('CHECK_PRIORTIME_STATUS',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/priorTimeAdjustment/{empId}")
	public ResponseEntity<List<ResponseModel>> priorTimeAdjustment(@PathVariable int empId,
			HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.checkPriorStatus(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_PRIORTIME_REQUEST')")
	@GetMapping("/getAllPriorTimeRequest")
	public ResponseEntity<Page<Map.Entry<Integer, List<Priortime>>>> getAllEmployeePriorTimeRequest(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		Page<Map.Entry<Integer, List<Priortime>>> pagedResult = timeSheetService.getAllEmployeePriorTimeRequest(page,
				size);
		return new ResponseEntity<>(pagedResult, HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_ATTENDANCE_BY_EMPLOYEE_ID',T(java.util.Map).of('currentUser', #empId))")
	@GetMapping("/empAttendence")
	public ResponseEntity<List<TimesheetDTO>> empAttendence(@RequestParam("empId") int empId,
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
			HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());

		return new ResponseEntity<>(timeSheetService.empAttendence(empId, fromDate, toDate), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_ALL_EMPLOYEE_ATTENDANCE')")
	@GetMapping("/allEmpAttendence")
	public ResponseEntity<List<TimeSheetModel>> allEmpAttendence(
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
			HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.allEmpAttendence(fromDate, toDate), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('PRIORTIME_ADJUSTMENT_REQUEST')")
	@PostMapping("/updatePriorTime")
	public ResponseEntity<ApiResponse> updatePriorTimeByDate(@RequestParam("Latitude") double latitude,
			@RequestParam("Longitude") double longitude,
			@RequestBody PriorTimeManagementRequest priorTimeManagementRequest, HttpServletRequest request)
			throws ParseException {

		LOGGER.info("API Call From IP: " + request.getRemoteHost());

		return ((Optional<Priortime>) timeSheetService.savePriorTime(priorTimeManagementRequest, latitude, longitude))
				.map(priorTimeuser -> {
					int priortimeId = priorTimeuser.getPriortimeId();
					UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.newInstance().scheme(scheme)
							.host(ipaddress).port(serverPort)
							.path(context + "/payroll/timeSheet/updatePriorTime/Accepted/" + priortimeId);
					UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.newInstance().scheme(scheme)
							.host(ipaddress).port(serverPort)
							.path(context + "/payroll/timeSheet/updatePriorTime/Rejected/" + priortimeId);
					OnPriorTimeDetailsSavedEvent onPriorTimeDetailsSavedEvent = new OnPriorTimeDetailsSavedEvent(
							priorTimeuser, urlBuilder1, urlBuilder2);
					applicationEventPublisher.publishEvent(onPriorTimeDetailsSavedEvent);

					return ResponseEntity.ok(new ApiResponse(true, "Mail sent successfully."));

				}).orElseThrow(() -> new PriorTimeAdjustmentException(priorTimeManagementRequest.getEmail(),
						"Missing user details in database"));
	}

	@PreAuthorize("@auth.allow('ACCEPT_PRIORTIME_REQUEST')")
	@GetMapping("/updatePriorTime/Accepted/{priortimeId}")
	public ResponseEntity<?> updatePriorTimeAccepted(@PathVariable(name = "priortimeId") int priortimeId,
			HttpServletRequest request) throws ParseException, TemplateNotFoundException,
			MalformedTemplateNameException, freemarker.core.ParseException, IOException, TemplateException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
		freemarkerConfig.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = freemarkerConfig.getTemplate("message.ftl");
		Map<String, Object> model = new HashMap<>();
		String status = "rejected";
		if (priortime.get().getStatus().equalsIgnoreCase("Pending")) {
			timeSheetService.saveConfirmedDetails(priortime);
			priortime.get().setStatus("Accepted");
			priortime.get().setUpdatedBy(Auth.updateByEmail);
			priorTimeRepository.save(priortime.get());
			String email = priortime.get().getEmail();
			OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent = new OnPriorTimeAcceptOrRejectEvent(priortime,
					"PriorTimesheet Entry", "Approved");
			applicationEventPublisher.publishEvent(onPriortimeApprovalEvent);
			model.put("Message", " priortime request has been successfully approved.!");
			model.put("Email", "");
			return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
					HttpStatus.OK);
		}
		if (priortime.get().getStatus().equalsIgnoreCase("Accepted")) {
			status = "approved";
		}
		model.put("Message", " Priortime request has been already Approved ");
		return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), HttpStatus.OK);

	}

	@PreAuthorize("@auth.allow('REJECT_PRIORTIME_REQUEST')")
	@GetMapping("/updatePriorTime/Rejected/{priortimeId}")
	public ResponseEntity<?> updatePriorTimeRejected(@PathVariable(name = "priortimeId") int priortimeId,
			HttpServletRequest request) throws TemplateNotFoundException, MalformedTemplateNameException,
			freemarker.core.ParseException, IOException, TemplateException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		Optional<Priortime> priortime = priorTimeRepository.findById(priortimeId);
		freemarkerConfig.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = freemarkerConfig.getTemplate("message.ftl");
		Map<String, Object> model = new HashMap<>();
		String status = "rejected";
		if (priortime.get().getStatus().equalsIgnoreCase("Pending")) {
			priortime.get().setStatus("Rejected");
			priortime.get().setUpdatedBy(Auth.updateByEmail);
			priorTimeRepository.save(priortime.get());
			OnPriorTimeAcceptOrRejectEvent onPriortimeApprovalEvent = new OnPriorTimeAcceptOrRejectEvent(priortime,
					"PriorTimesheet Entry", "Rejected");
			applicationEventPublisher.publishEvent(onPriortimeApprovalEvent);
			model.put("Message", " priortime request has been successfully rejected.!");
			model.put("Email", "");
			return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
					HttpStatus.OK);
		}
		if (priortime.get().getStatus().equalsIgnoreCase("Accepted")) {
			status = "approved";
		}
		model.put("Message", "Priortime request has been already Rejected");
		return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), HttpStatus.OK);

	}

	@PreAuthorize("@auth.allow('PAUSE_TIME',T(java.util.Map).of('currentUser', #empId))")
	@PutMapping("/pause/{empId}")
	public ResponseEntity<String> pauseWorkingTime(@PathVariable int empId, HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.pauseWorkingTime(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('RESUME_TIME',T(java.util.Map).of('currentUser', #empId))")
	@PatchMapping("/resume/{empId}")
	public ResponseEntity<String> resumeWorkingTime(@PathVariable int empId, HttpServletRequest request)
			throws ParseException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return new ResponseEntity<>(timeSheetService.resumeWorkingTime(empId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('EMPLOYEE_EXPENSE_SAVE_REQUEST')")
	@PostMapping("/employeeExpense/{empId}")
	public ResponseEntity employeeExpense(@PathVariable int empId, @RequestParam("expense") String expense,
			@RequestParam List<MultipartFile> invoice, HttpServletRequest request) throws ParseException, IOException {

		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		Gson gson = new Gson();
		EmployeeExpense employeeExpenseRequest = gson.fromJson(expense, EmployeeExpense.class);
		EmployeeExpenseDTO employeeExpenseDTO = timeSheetService.employeeExpense(empId, invoice,
				employeeExpenseRequest);

		UriComponentsBuilder urlBuilder1 = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/timeSheet/employeeExpenseApprove/" + employeeExpenseDTO.getExpenseId());
		UriComponentsBuilder urlBuilder2 = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/timeSheet/employeeExpenseReject/" + employeeExpenseDTO.getExpenseId());
		OnEmployeeExpenseDetailsSavedEvent onEmployeeExpenseDetailsSavedEvent = new OnEmployeeExpenseDetailsSavedEvent(
				employeeExpenseDTO, urlBuilder1, urlBuilder2);
		applicationEventPublisher.publishEvent(onEmployeeExpenseDetailsSavedEvent);
		return ResponseEntity.ok(new ApiResponse(true, "Expense Submitted Successfully."));
	}

	@PreAuthorize("@auth.allow('APPROV_EEMPLOYEE_EXPENSE')")
	@PutMapping("/employeeExpenseApprove/{expenseId}")
	public ResponseEntity<String> ApproveEmployeeExpense(@PathVariable int expenseId, HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(timeSheetService.approveEmployeeExpenseById(expenseId));

	}

	@PutMapping("/employeeExpenseReject/{expenseId}")
	public ResponseEntity<String> RejectEmployeeExpense(@PathVariable int expenseId, HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(timeSheetService.rejectEmployeeExpenseById(expenseId));

	}

	@PreAuthorize("@auth.allow('UPDATE_EMPLOYEE_EXPENSE')")
	@PutMapping("/employeeExpense")
	public ResponseEntity<ApiResponse> EmployeeExpense(@RequestBody EmployeeExpenseDTO employeeExpenseDTO,
			HttpServletRequest request) throws ParseException, IOException {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		EmployeeExpenseDTO employeeExpenseDTO1 = timeSheetService
				.acceptedEmployeeExpense(employeeExpenseDTO.getExpenseId(), employeeExpenseDTO);
		OnEmployeeExpenseAcceptOrRejectEvent onEmployeeExpenseAcceptOrRejectEvent = new OnEmployeeExpenseAcceptOrRejectEvent(
				employeeExpenseDTO, "Expenses Entry", "Approved");
		applicationEventPublisher.publishEvent(onEmployeeExpenseAcceptOrRejectEvent);
		return ResponseEntity.ok(new ApiResponse(true, "Details for Employee Expense updated successfully"));
	}

	@GetMapping("/getAllExpense")
	public ResponseEntity<List<EmployeeExpense>> getAllExpenseDetail(HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		return ResponseEntity.ok(timeSheetService.getAllExpenseDetail());

	}

	@PreAuthorize("@auth.allow('EXPORT_ALL_EMP_ATTENDANCE_TO_EXCEL')")
	@GetMapping("/exporttoexcel")
	public ResponseEntity<Resource> allEmpAttendanceExportExcel(
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) throws IOException {
		String filename = "alphadot_attendance_data.xlsx";

		ByteArrayInputStream newdata = timeSheetService.getExcelData(fromDate, toDate);

		InputStreamResource file = new InputStreamResource(newdata);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
	}

	@PreAuthorize("@auth.allow('RESEND_PRIORTIME_REQUEST')")
	@PutMapping("/reSendPriorTimeRequest/{priortimeId}")
	public ResponseEntity<String> reSendLeaveRequest(@PathVariable("priortimeId") int priortimeId) {
		LOGGER.info("Payroll service: leave:  RejectLeaveRequest Info level log msg");
		return new ResponseEntity<>(timeSheetService.reSendPriorTimeRequest(priortimeId), HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('EARLY_CHECK_OUT_BY_EMPID')")
	@PutMapping("/earlyCheckOut/{empId}")
	public ResponseEntity<String> earlyCheckOut(@RequestParam("Latitude") double latitude,
			@RequestParam("Longitude") double longitude, @PathVariable int empId, @RequestParam("reason") String reason,
			@RequestParam("type") String type) throws ParseException {
		return new ResponseEntity<>(timeSheetService.earlyCheckOut(latitude, longitude, empId, reason, type),
				HttpStatus.OK);
	}

	@PreAuthorize("@auth.allow('GET_PRIOR_TIME_HISTORY_BY_EMPID')")
	@GetMapping("/getPriorTimeHistory/{employeeId}")
	public ResponseEntity<List<Priortime>> getPriorTimeHistoryByEmployeeId(@PathVariable int employeeId) {
		List<Priortime> priortimeHistory = priortimeService.getPriorTimeHistoryByEmployeeId(employeeId);
		return ResponseEntity.ok(priortimeHistory);
	}

	@PreAuthorize("@auth.allow('UPDATE_CHECK_IN_CHECK_OUT_BY_EMP_ID')")
	@PostMapping("/updateCheckInCheckOut/{empId}")
	public ResponseEntity<String> updateCheckInCheckOut(@PathVariable int empId,
			@RequestParam(value = "checkIn", required = false) String checkInTime,
			@RequestParam(value = "checkOut", required = false) String checkOutTime,
			@RequestParam(value = "date", required = false) String date, HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		String response = timeSheetService.updateCheckInCheckOutByEmpId(empId, checkInTime, checkOutTime, date);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("@auth.allow('GET_EMPLOYEE_BY_ID_AND_DATE')")
	@GetMapping("/getEmpByIdAndDate")
	public ResponseEntity<String> getTimeSheetByEmpIdAndDate(@RequestParam(value = "employeeId") int employeeId,
			@RequestParam(value = "date") String date) {
		Optional<TimeSheetModel> timeSheet = timeSheetService.getTimeSheetByEmployeeIdAndDate(employeeId, date);
		if (timeSheet.isPresent()) {
			return ResponseEntity.ok(timeSheet.get().toString());
		} else {
			return ResponseEntity.status(404).body("No time sheet entry found for the given employee and date.");
		}
	}

	@PreAuthorize("@auth.allow('SEARCH_PRIORTIME_BY_DATE')")
	@GetMapping("/getPriorTimeDetailsByDate")
	public ResponseEntity<Page<Priortime>> getPriorTimeByDateRange(@RequestParam(value = "from") String fromDate,
			@RequestParam(value = "to") String toDate, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		try {
			Page<Priortime> pagedResult = priortimeService.getPriorTimeDetailsByDateRange(fromDate, toDate, page, size);
			if (pagedResult.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			return ResponseEntity.ok(pagedResult);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

// running code(gateway+postman) but URL not build on click button(mail), so will check after completing other task----
////	@PreAuthorize("@auth.allow('EMPLOYEE_COMPOFF_APPROVED_OR_REJECTED')")
//	@GetMapping("/empCompOffApprovedOrRejected/{empId}/{compOffDate}/{compOffStatus}")
//	public ResponseEntity<String> empCompOffApprovedOrRejected(@PathVariable(value = "empId") Integer empId,
//			@PathVariable(value = "compOffDate") String compOffDate,
//			@PathVariable(value = "compOffStatus") String compOffStatus, HttpServletRequest request)
//			throws TemplateException, MessagingException, IOException, ParseException {
//		try {
//			LOGGER.info("API Call From IP: " + request.getRemoteHost());
////			Date compOffDateConverted = new SimpleDateFormat("yyyy-MM-dd").parse(compOffDate);
////			CompOff compOffExist = compOffRepo.findCompOffByEmployeeIdAndDate(empId, compOffDateConverted);
//
//			Optional<User> user = Optional.ofNullable(userRepo.findByEmployeeId(empId)
//					.orElseThrow(() -> new NoDataFoundException("Employee not found with EmpId:" + empId)));
//
//			String name = user.get().getFirstName() + " " + user.get().getLastName();
//
//			Date compOffDateConverted = new SimpleDateFormat("yyyy-MM-dd").parse(compOffDate);
//			Optional<CompOff> compOffExist = Optional.ofNullable(compOffRepo
//					.findByEmpIdAndCompOffDate(empId, compOffDateConverted).orElseThrow(() -> new NoDataFoundException(
//							"CompOff data not found for empName:" + name + " of compOffDate:" + compOffDate)));
//
//			freemarkerConfig.setClassForTemplateLoading(getClass(), basePackagePath);
//			Template template = freemarkerConfig.getTemplate("comp_off_message.ftl");
//			Map<String, Object> model = new HashMap<>();
//			String status = compOffExist.get().getStatus();
//
//			if (status.equalsIgnoreCase("Pending") && compOffStatus.equalsIgnoreCase("Rejected")) {
//				LOGGER.info("Payroll service: empCompOffApprovedOrRejected Info level log msg");
//				timeSheetService.empCompOffApprovedOrRejected(empId, compOffDate, compOffStatus);
//				model.put("Message", "Comp-Off request has been " + compOffStatus + " !!");
////				model.put("Email", "");
//				return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
//						HttpStatus.OK);
//			}
//			if (status.equalsIgnoreCase("Pending") && compOffStatus.equalsIgnoreCase("Approved")) {
//				LOGGER.info("Payroll service: empCompOffApprovedOrRejected Info level log msg");
//				timeSheetService.empCompOffApprovedOrRejected(empId, compOffDate, compOffStatus);
//				model.put("Message", "Comp-Off request has been " + compOffStatus + "!!");
////				model.put("Email", "");
//				return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
//						HttpStatus.OK);
//			}
//			model.put("Message", "Comp-Off request has been already " + status + " !!");
////			model.put("Email", status);
//			return new ResponseEntity<>(FreeMarkerTemplateUtils.processTemplateIntoString(template, model),
//					HttpStatus.OK);
//		} catch (NoDataFoundException e) {
//			LOGGER.error("Error occurred: ", e);
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
//		} catch (Exception e) {
//			LOGGER.error("Error occurred: ", e);
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	@PreAuthorize("@auth.allow('GET_ALL_APPROVED_COMP_OFF_DATA')")
	@GetMapping("/getAllApprovedCompOffData")
	public ResponseEntity<Object> getAllApprovedCompOffData(HttpServletRequest request) {
		LOGGER.info("API Call From IP: " + request.getRemoteHost());
		ResponseDTO responseDTO = timeSheetService.getAllApprovedCompOffData();
		HttpStatus status = HttpStatusMapper.mapToHttpStatus(responseDTO.getStatus());
		return new ResponseEntity<>(responseDTO, status);
	}

}
