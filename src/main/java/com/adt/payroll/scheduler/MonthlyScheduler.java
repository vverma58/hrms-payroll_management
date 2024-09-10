package com.adt.payroll.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import com.adt.payroll.model.LeaveBalance;
import com.adt.payroll.repository.LeaveBalanceRepository;
//import jakarta.transaction.Transactional;
import com.adt.payroll.service.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.adt.payroll.exception.NoDataFoundException;
import com.adt.payroll.model.LeaveModel;
import com.adt.payroll.model.TimeSheetModel;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.LeaveRepository;
import com.adt.payroll.repository.TimeSheetRepo;
import com.adt.payroll.repository.UserRepo;
import com.adt.payroll.service.CommonEmailService;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MonthlyScheduler {

	private static final Logger log = LogManager.getLogger(MonthlyScheduler.class);

	@Autowired
	private Util util;

	@Autowired
	private LeaveRepository leaveRepository;

	@Autowired
	private TimeSheetRepo timeSheetRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CommonEmailService mailService;
	@Autowired
	private LeaveBalanceRepository leaveBalanceRepo;

	public void updateAbsentDaysInDatabase() {
		timeSheetRepo.updateAbsentDays();
	}

	@Value("${holiday}")
	private String[] holiday;

	@Scheduled(cron = "0 0 0 1 * *") // Executes on the 1st day of each month at midnight
	public void updateColumnValue() {
		List<LeaveModel> leaveModelList = leaveRepository.findAll();
		for (LeaveModel lm : leaveModelList) {
			lm.setLeaveBalance(lm.getLeaveBalance() + 1);
			leaveRepository.save(lm);
		}

	}

//	@Scheduled(cron = "*/2 * * * * *") // for 2 seconds
	@Scheduled(cron = "0 0 8 * * MON") // Executes on the every Monday at 8 AM
	public void sendNotificationForTimeSheet() {
		log.info("Generate weekly time sheet report ");
		LocalDate currentDate = LocalDate.now();
		LocalDate endDate = currentDate.minusDays(1);
		LocalDate startDate = currentDate.minusDays(7);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		List<TimeSheetModel> timeSheet = timeSheetRepo.findTimeSheetWithNullValues(startDate.format(formatter),
				endDate.format(formatter));

		if (!timeSheet.isEmpty() && timeSheet.size() > 0) {
			Map<Integer, List<TimeSheetModel>> employeeTimeSheetDetails = timeSheet.stream()
					.collect(Collectors.groupingBy(TimeSheetModel::getEmployeeId));

			employeeTimeSheetDetails.forEach((i, e) -> {
				try {
					Optional<User> user = Optional.ofNullable(userRepo.findById(i)
							.orElseThrow(() -> new NoDataFoundException("employee not found :" + i)));

					ByteArrayOutputStream employeeReport = generateExcelReport(e, user.get().getAdtId());
					log.info("Sheet generated for employee {}: ",
							user.get().getFirstName() + " " + user.get().getLastName());
					mailService.sendEmailForTimeSheet(employeeReport,
							user.get().getFirstName() + " " + user.get().getLastName(), user.get().getEmail(),
							startDate.format(formatter) + " to " + endDate.format(formatter));

				} catch (IOException ex) {
					log.error("Error while generating timesheet report. {} ", ex.getMessage());
				}
			});
		}
	}

	private ByteArrayOutputStream generateExcelReport(List<TimeSheetModel> timeSheet, String adtId) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("TimeSheet Report");

		Row headerRow = sheet.createRow(0);
		String[] headers = { "ID", "Check-In", "Check-Out", "Working Hour", "Date", "Day" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
		}
		int rowNum = 1;
		for (TimeSheetModel timeSheetModel : timeSheet) {
			Row row = sheet.createRow(rowNum++);
			// row.createCell(0).setCellValue(String.valueOf(timeSheetModel.getEmployeeId()));
			row.createCell(0).setCellValue(adtId);
			row.createCell(1).setCellValue(timeSheetModel.getCheckIn() != null ? timeSheetModel.getCheckIn() : "NULL");
			row.createCell(2)
					.setCellValue(timeSheetModel.getCheckOut() != null ? timeSheetModel.getCheckOut() : "NULL");
			row.createCell(3)
					.setCellValue(timeSheetModel.getTotalWorkingHours() != null
							? timeSheetModel.getTotalWorkingHours().toString()
							: "NULL");
			row.createCell(4).setCellValue(timeSheetModel.getDate() != null ? timeSheetModel.getDate() : "NULL");
			row.createCell(5).setCellValue(timeSheetModel.getDay() != null ? timeSheetModel.getDay() : "NULL");
		}

		try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
			workbook.write(fileOut);
			workbook.close();
			return fileOut;
		}

	}

	@Scheduled(cron = "0 0 8 * * MON")
	public void sendLeaveNotificationForTimesheet() {
		log.info("Generate timesheet report and mark absence for employees who were absent during working days");

		List<LocalDate> workingDates = util.getWorkingDaysOfPreviousAndCurrentMonth();

		LocalDate[] dates = calculateStartAndEndDates(workingDates);
		LocalDate startDate = dates[0];
		LocalDate endDate = dates[1];

		log.info("Calculated start date: {}", startDate);
		log.info("Calculated end date: {}", endDate);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedStartDate = startDate.format(formatter);
		String formattedEndDate = endDate.format(formatter);
		log.info("Start Date: {}, End Date: {}", formattedStartDate, formattedEndDate);

		List<TimeSheetModel> timeSheet = timeSheetRepo.findTimeSheetWithValues(formattedStartDate, formattedEndDate);

		if (!timeSheet.isEmpty()) {
			LocalDate[] weekDates = calculateStartAndEndDates(workingDates);
			List<LocalDate> workingDatesInWeek = workingDates.stream()
					.filter(date -> !date.isBefore(weekDates[0]) && !date.isAfter(weekDates[1]))
					.collect(Collectors.toList());

			processTimeSheets(timeSheet, workingDatesInWeek);
		} else {
			log.info("No timesheets found for the current week.");
		}
	}

	private LocalDate[] calculateStartAndEndDates(List<LocalDate> workingDates) {
		LocalDate currentDate = LocalDate.now();
		LocalDate endDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1);
		LocalDate startDate = endDate.minusDays(6);

		while (!workingDates.contains(endDate) && endDate.isAfter(startDate)) {
			endDate = endDate.minusDays(1);
		}
		while (!workingDates.contains(startDate) && startDate.isBefore(endDate)) {
			startDate = startDate.plusDays(1);
		}

		return new LocalDate[] { startDate, endDate };
	}

	private void processTimeSheets(List<TimeSheetModel> timeSheets, List<LocalDate> workingDatesInWeek) {
		Map<Integer, List<TimeSheetModel>> employeeTimeSheetDetails = timeSheets.stream()
				.collect(Collectors.groupingBy(TimeSheetModel::getEmployeeId));

		for (Map.Entry<Integer, List<TimeSheetModel>> entry : employeeTimeSheetDetails.entrySet()) {
			Integer employeeId = entry.getKey();
			List<TimeSheetModel> timeSheetsForEmployee = entry.getValue();

			try {
				List<LocalDate> absentDates = markAbsences(timeSheetsForEmployee, workingDatesInWeek, employeeId);
				if (absentDates.isEmpty()) {
					log.info("No absences recorded for employee: {}", employeeId);
				} else {
					log.info("Absence recorded successfully for employee: {}. Absent Dates: {}", employeeId,
							absentDates);
				}
			} catch (Exception ex) {
				log.error("Error processing leave notification for employeeId: {}. Exception message: {}", employeeId,
						ex.getMessage(), ex);
			}
		}
	}

	private List<LocalDate> markAbsences(List<TimeSheetModel> timeSheets, List<LocalDate> workingDatesInWeek,
			Integer employeeId) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		Set<LocalDate> presentDates = timeSheets.stream().map(timeSheet -> {
			try {
				return LocalDate.parse(timeSheet.getDate(), formatter);
			} catch (DateTimeParseException e) {
				log.error("Error parsing date from TimeSheetModel: {}", timeSheet.getDate(), e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toSet());

		log.info("Dates with records: {}", presentDates);

		List<LocalDate> absentDates = workingDatesInWeek.stream().filter(date -> !presentDates.contains(date))
				.collect(Collectors.toList());

		List<User> activeEmployees = userRepo.findAllByIsActive(true);
		Set<Integer> employeeIdsWithTimeSheets = timeSheets.stream().map(TimeSheetModel::getEmployeeId)
				.collect(Collectors.toSet());

		for (User employee : activeEmployees) {
			Integer empId = employee.getId();
			if (!employeeIdsWithTimeSheets.contains(empId)) {
				markAbsenceForEmployee(empId, workingDatesInWeek, formatter);
			}
		}

		if (!absentDates.isEmpty()) {
			absentDates.forEach(absentDate -> {
				try {
					TimeSheetModel absenceRecord = createAbsenceRecord(employeeId, absentDate, formatter);
					timeSheetRepo.save(absenceRecord);
					log.info("Marked absence for employeeId: {} on date: {}", employeeId, absentDate);
				} catch (Exception ex) {
					log.error("Error marking absence for employeeId: {} on date: {}. Exception message: {}", employeeId,
							absentDate, ex.getMessage(), ex);
				}
			});
		}

		return absentDates;
	}

	private void markAbsenceForEmployee(Integer employeeId, List<LocalDate> workingDatesInWeek,
			DateTimeFormatter formatter) {
		workingDatesInWeek.forEach(date -> {
			try {
				Optional<TimeSheetModel> existingRecords = timeSheetRepo.findByEmployeeIdAndDate(employeeId,
						date.format(formatter));
				if (existingRecords.isEmpty()) {
					TimeSheetModel absenceRecord = createAbsenceRecord(employeeId, date, formatter);
					timeSheetRepo.save(absenceRecord);
					log.info("Marked absence for employeeId: {} on date: {}", employeeId, date);
				} else {
					log.info("Absence record already exists for employeeId: {} on date: {}", employeeId, date);
				}
			} catch (Exception ex) {
				log.error("Error marking absence for employeeId: {} on date: {}. Exception message: {}", employeeId,
						date, ex.getMessage(), ex);
			}
		});
	}

	private TimeSheetModel createAbsenceRecord(Integer employeeId, LocalDate date, DateTimeFormatter formatter) {
		DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

		TimeSheetModel absenceRecord = new TimeSheetModel();
		absenceRecord.setEmployeeId(employeeId);
		absenceRecord.setDate(date.format(formatter));
		absenceRecord.setStatus("Absent");
		String monthInUpperCase = date.format(monthFormatter).toUpperCase();
		absenceRecord.setMonth(monthInUpperCase);
		absenceRecord.setDay(date.format(dayFormatter));
		absenceRecord.setYear(String.valueOf(date.getYear()));

		return absenceRecord;
	}
}

//	@Scheduled(cron = "0 */1 * * * *")
// @Scheduled(cron = "0 0 0 28-31 * ?") // Executes at midnight on the 28th to
// 31st of every month
//	@Transactional
//	public String sendLeaveNotificationOnMonthlyBasis() {
//		LocalDate currentDate = LocalDate.now();
//		LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
//		// This block ensures the task runs only on the last day of the month
//		if (!currentDate.isEqual(lastDayOfMonth)) {
//			return "Not the last day of the month, task not executed.";
//		}
//		// Fetch all users
//		List<User> users = userRepo.findAll();
//		users.forEach(user -> {
//			try {
//				int employeeId = user.getId();
//				Optional<LeaveBalance> leaveBalanceOpt = Optional.ofNullable(leaveBalanceRepo.findByEmpId(employeeId));
//				LeaveBalance leaveBalance = leaveBalanceOpt.orElseGet(() -> {
//					LeaveBalance newLeaveBalance = new LeaveBalance();
//					newLeaveBalance.setEmpId(employeeId);
//					newLeaveBalance.setLeaveBalance(1); // Initialize paid leave to 0
//					return newLeaveBalance;
//				});
//				// Allocate 1 paid leave at the start of the month
//				leaveBalance.setLeaveBalance(leaveBalance.getPaidLeave() + 1);
//				// Save the updated leave balance
//				leaveBalanceRepo.save(leaveBalance);
//				log.info("Leave notification and balance updated successfully for employee: {} {}", user.getFirstName(), user.getLastName());
//			} catch (Exception ex) {
//				log.error("Error processing leave notification for employeeId: {}. Exception message: {}", user.getId(), ex.getMessage(), ex);
//			}
//		});
//		return "Leave notifications and balances sent successfully for users.";
//	}
