package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import com.adt.payroll.config.Auth;
import com.adt.payroll.dto.EmployeeExpenseDTO;
import com.adt.payroll.event.OnCompOffDetailsSavedEvent;
import com.adt.payroll.event.OnEmpCompOffApproveOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseAcceptOrRejectEvent;
import com.adt.payroll.event.OnEmployeeExpenseDetailsSavedEvent;
import com.adt.payroll.event.OnLeaveAcceptOrRejectEvent;
import com.adt.payroll.event.OnLeaveCancelEvent;
import com.adt.payroll.event.OnPriorTimeAcceptOrRejectEvent;
import com.adt.payroll.event.OnPriorTimeDetailsSavedEvent;
import com.adt.payroll.model.CompOff;
import com.adt.payroll.model.IsEmailSend;
import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestCancelEvent;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;
import com.adt.payroll.model.Priortime;
import com.adt.payroll.model.User;
import com.adt.payroll.repository.IsEmailSendRepository;
import com.adt.payroll.repository.UserRepo;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class CommonEmailServiceImpl implements CommonEmailService {

	private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private IsEmailSendRepository isEmailSendRepo;

	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Value("${spring.mail.username}")
	private String sender;

	@Value("${spring.mail.username}")
	private String mailFrom;

	@Value("${spring.mail.cc}")
	private String ccEmail;
	private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private Configuration templateConfiguration;

	@Autowired
	private final JavaMailSender mailSender;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private TableDataExtractor dataExtractor;

	@Value("${utility.service.url}")
	private String utilityServiceUrl;

	@Autowired
	private Auth auth;

	public CommonEmailServiceImpl(JavaMailSender mailSender, Configuration templateConfiguration) {
		this.mailSender = mailSender;
		this.templateConfiguration = templateConfiguration;
	}

	// *** START:- For Prior Time ***
	/**
	 * Send email verification to the user and persist the token in the database.
	 */
	@Override
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event) {
		Priortime priortime = event.getPriorTime();
		String recipientAddress = priortime.getEmail();
		String emailConfirmationUrl1 = event.getRedirectUrl1().toUriString();
		String emailConfirmationUrl2 = event.getRedirectUrl2().toUriString();

		try {
			sendEmailVerification(event, emailConfirmationUrl1, emailConfirmationUrl2, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}

	/**
	 * Send email verification to the user and persist the token in the database.
	 */
	@Override
	public void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event) {
		log.info("sendAccountChangeEmailRejected");
		String action = event.getAction();
		String actionStatus = event.getActionStatus();
		String recipientAddress = event.getPriortime().get().getEmail();
		try {
			sendAccountChangeEmail(event, action, actionStatus, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}

	@Override
	public void sendLeaveAcceptAndRejectedEmail(OnLeaveAcceptOrRejectEvent event) {
		log.info("sendAccountChangeEmailRejected");
		String recipientAddress = null;
		try {
			sendleaveResponseEmail(event, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			throw new MailSendException(recipientAddress);
		}
	}

	@Override
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String from) throws IOException, TemplateException, MessagingException {
		String sql = "select * from av_schema.priortime_email";
		List<Map<String, Object>> priortimeData = dataExtractor.extractDataFromTable(sql);
		for (Map<String, Object> priortime : priortimeData) {
			String email = String.valueOf(priortime.get("email_id"));
			String token = auth.tokenGanreate(email);
			Mail mail = new Mail();
			mail.setTo(email);
			mail.setSubject("Email Verification [Team CEP]");
			mail.setFrom(from);
			mail.getModel().put("approveLeaveRequestLink1", emailVerificationUrl1 + "?Authorization=" + token);
			mail.getModel().put("RejectLeaveRequestLink2", emailVerificationUrl2 + "?Authorization=" + token);
			mail.getModel().put("Email", event.getPriorTime().getEmail());
			mail.getModel().put("CheckInTime", event.getPriorTime().getCheckIn());
			mail.getModel().put("CheckOutTime", event.getPriorTime().getCheckOut());
			mail.getModel().put("Date", event.getPriorTime().getDate());
			mail.getModel().put("Month", event.getPriorTime().getMonth());
			mail.getModel().put("Year", event.getPriorTime().getYear());
			mail.getModel().put("EmployeeId", String.valueOf(event.getPriorTime().getEmployeeId()));

			templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
			Template template = templateConfiguration.getTemplate("priortime_email_verification.ftl");
			String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());

			mail.setContent(mailContent);
			send(mail);
		}

	}

	@Override
	public void sendAccountChangeEmail(OnPriorTimeAcceptOrRejectEvent event, String action, String actionStatus,
			String to) throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
		mail.setSubject("Timesheet Saved");
		mail.setTo(to);
		mail.setFrom(sender);
		mail.getModel().put("userName", to);
		mail.getModel().put("action", action);
		mail.getModel().put("actionStatus", actionStatus);
		mail.getModel().put("CheckIn", event.getPriortime().get().getCheckIn());
		mail.getModel().put("CheckOut", event.getPriortime().get().getCheckOut());
		mail.getModel().put("Date", event.getPriortime().get().getDate());
		mail.getModel().put("Email", event.getPriortime().get().getEmail());
		mail.getModel().put("Month", event.getPriortime().get().getMonth());
		mail.getModel().put("Year", event.getPriortime().get().getYear());
		mail.getModel().put("WorkingHour", event.getPriortime().get().getWorkingHour());

		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("account-activity-change.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);

	}

	@Override
	public void send(Mail mail) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		if (mail.getToArray() != null && !mail.getToArray().isEmpty()) {
			String[] emailArray = mail.getToArray().toArray(new String[0]);
			helper.setTo(emailArray);
		} else {
			helper.setTo(mail.getTo());
		}
		helper.setText(mail.getContent(), true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		mailSender.send(message);
	}
	// *** END:- For Prior Time ***

	// *** START:- To create payslip and then send mail ***
	@Async
	@Override
	public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear) {
		log.info("sendEmail started");
		String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
				.replace("[Month, Year]", monthYear);
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(gmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);
			mimeMessageHelper.addAttachment(name + ".pdf", source);
			log.info("sendEmail end");
			mailSender.send(mimeMessage);

			log.info("Mail send Successfully");
		} catch (MessagingException e) {
			log.error("getting error while send email=" + e.getMessage());

		}
	}
	// *** END:- To create payslip and then send mail ***

	// *** START:- To send mail for Leave Request ***
	@Override
	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr)
			throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
		mail.setSubject("Leave Request");
		// *** From whom the mail should come ***
		Integer empID = lr.getEmpid();
		Optional<User> user = userRepo.findById(empID);
		String userEmail = user.get().getEmail();
		mail.setFrom(userEmail);
		String sql = "select * from av_schema.priortime_email";
		List<String> emailArray = new ArrayList<>();
		List<Map<String, Object>> priortimeData = dataExtractor.extractDataFromTable(sql);
		for (Map<String, Object> priortime : priortimeData) {
			String email = String.valueOf(priortime.get("email_id"));
			String token = auth.tokenGanreate(email);
			mail.setTo(email);
			mail.getModel().put("leaveApprovalLink", Url + "?Authorization=" + token);
			mail.getModel().put("leaveRejectionLink", Url1 + "?Authorization=" + token);
			mail.getModel().put("LeaveId", event.getLeaveRequestModel().getLeaveid().toString());
			mail.getModel().put("EmpId", event.getLeaveRequestModel().getEmpid().toString());
			mail.getModel().put("Name", user.get().getFirstName() + " " + user.get().getLastName());
			mail.getModel().put("LeaveType", event.getLeaveRequestModel().getLeaveType());
			mail.getModel().put("Reason", event.getLeaveRequestModel().getLeaveReason());
			mail.getModel().put("LeaveDates", event.getLeaveRequestModel().getLeavedate().toString());
			mail.getModel().put("Status", event.getLeaveRequestModel().getStatus());
			try {
				templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
				Template template = templateConfiguration.getTemplate("leave_status_change.ftl");
				String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
				mail.setContent(mailContent);
				String url = utilityServiceUrl + "/emails/send";
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON); // Correctly set Content-Type
				HttpEntity<Mail> request = new HttpEntity<>(mail, headers);
				restTemplate.postForEntity(url, request, String.class);
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			} catch (TemplateException | IOException e) {
				log.error("Failed to process email template", e);
				throw new MessagingException("Failed to process email template", e);
			} catch (Exception e) {
				log.error("Failed to send email", e);
				throw new MessagingException("Failed to send email", e);
			}

		}
		return "Mail Sent Successfully";
	}

	@Override
	public void sendEmail(OnLeaveRequestSaveEvent event) {
		LeaveRequestModel leaveRequestModel = event.getLeaveRequestModel();
		String emailApprovalUrl = event.getRedirectUrl().toUriString();
		String emailRejectionUrl = event.getRedirectUrl1().toUriString();

		try {
			sendEmail(event, emailApprovalUrl, emailRejectionUrl, leaveRequestModel);
		} catch (IOException | TemplateException | MessagingException e) {
			e.printStackTrace();
		}
	}

	public void sendEmployeeExpenseVerification(OnEmployeeExpenseDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException {
		EmployeeExpenseDTO employeeExpenseDTO = event.getEmployeeExpenseDTO();
		Mail mail = new Mail();
		mail.setSubject("Employee Expense Request...");
		mail.setTo(to);
		mail.setFrom(mailFrom);
		mail.getModel().put("expenseId", employeeExpenseDTO.getExpenseId() + "");
		mail.getModel().put("EmployeeName", employeeExpenseDTO.getEmpName());
		mail.getModel().put("approveEmployeeExpenseLink1", emailVerificationUrl1);
		mail.getModel().put("rejectEmployeeExpenseLink1", emailVerificationUrl2);
		mail.getModel().put("Email", to);
		mail.getModel().put("expenseAmount", employeeExpenseDTO.getExpenseAmount());
		mail.getModel().put("comments", employeeExpenseDTO.getEmployeeComments());
		mail.getModel().put("expenseDescription", employeeExpenseDTO.getExpenseDescription());
		mail.getModel().put("expenseCategory", employeeExpenseDTO.getExpenseCategory());
		mail.getModel().put("paymentDate", employeeExpenseDTO.getPaymentDate());
		mail.getModel().put("paymentMode", employeeExpenseDTO.getPaymentMode());
		mail.getModel().put("submitDate", employeeExpenseDTO.getSubmitDate());
		if (employeeExpenseDTO.getInvoices() != null) {
			mail.setAttachments(employeeExpenseDTO.getInvoices());
		}
		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("employee_expense_request.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);
	}

	public void sendEmployeeExpenseApprovalEmail(OnEmployeeExpenseAcceptOrRejectEvent event, String action,
			String actionStatus, String to) throws IOException, TemplateException, MessagingException {
		EmployeeExpenseDTO employeeExpenseDTO = event.getEmployeeExpenseDTO();
		Mail mail = new Mail();
		mail.setSubject("Expense Approval");
		mail.setTo(to);
		mail.setFrom(mailFrom);
		mail.getModel().put("employeeName", employeeExpenseDTO.getEmpName());
		mail.getModel().put("Email", to);
		mail.getModel().put("action", action);
		mail.getModel().put("actionStatus", actionStatus);
		mail.getModel().put("expenseAmount", employeeExpenseDTO.getExpenseAmount());
		mail.getModel().put("Comments", employeeExpenseDTO.getEmployeeComments());
		mail.getModel().put("expenseDescription", employeeExpenseDTO.getExpenseDescription());
		mail.getModel().put("expenseCategory", employeeExpenseDTO.getExpenseCategory());
		mail.getModel().put("paymentDate", employeeExpenseDTO.getPaymentDate());
		mail.getModel().put("paymentMode", employeeExpenseDTO.getPaymentMode());
		mail.getModel().put("submitDate", employeeExpenseDTO.getSubmitDate());
		if (employeeExpenseDTO.getInvoices() != null) {
			mail.setAttachments(employeeExpenseDTO.getInvoices());
		}
		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("employee_expense_approve.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);
	}

	public void sendleaveResponseEmail(OnLeaveAcceptOrRejectEvent event, String to)
			throws IOException, TemplateException, MessagingException {
		Mail mail = new Mail();
		Optional<User> user = userRepo.findById(event.getLeaveInfo().get().getEmpid());

		mail.setSubject("Leave Request Status");
		mail.setFrom(mailFrom);
		mail.setTo(event.getLeaveInfo().get().getEmail());
		mail.getModel().put("Message", event.getLeaveInfo().get().getMessage());
		mail.getModel().put("Name", user.get().getFirstName() + " " + user.get().getLastName());
		mail.getModel().put("LeaveType", event.getLeaveInfo().get().getLeaveType());
		mail.getModel().put("Reason", event.getLeaveInfo().get().getLeaveReason());
		mail.getModel().put("LeaveDates", event.getLeaveInfo().get().getLeavedate().toString());
		mail.getModel().put("Status", event.getLeaveInfo().get().getStatus());
		try {
			templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
			Template template = templateConfiguration.getTemplate("approve_and_reject_leave_request.ftl");
			String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
			mail.setContent(mailContent);
			String url = utilityServiceUrl + "/emails/send";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON); // Correctly set Content-Type
			HttpEntity<Mail> request = new HttpEntity<>(mail, headers);
			restTemplate.postForEntity(url, request, String.class);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (TemplateException | IOException e) {
			log.error("Failed to process email template", e);
			throw new MessagingException("Failed to process email template", e);
		} catch (Exception e) {
			log.error("Failed to send email", e);
			throw new MessagingException("Failed to send email", e);
		}
	}

	@Override
	public void sendAccountChangeEmailApproved(OnEmployeeExpenseAcceptOrRejectEvent event)
			throws IOException, TemplateException, MessagingException {
		// TODO Auto-generated method stub

	}

	@Async
	@Override
	public void sendEmail(String name) {
		String massage = Util.MESSAGE.replace("[Name]", name);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			// DataSource source = new ByteArrayDataSource(baos.toByteArray(),
			// "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(ccEmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip");
			// mimeMessageHelper.addAttachment(name + ".pdf", source);
			mailSender.send(mimeMessage);

			log.info("Mail send Successfully to team hr.");
		} catch (MessagingException e) {
			log.info("Error");

		}
	}

	@Async
	@Override
	public void sendEmail(String name, String msg) {
		String massage = Util.ERR_MESSAGE.replace("[Name]", name).replace("[errorMsg]", msg);
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			// DataSource source = new ByteArrayDataSource(baos.toByteArray(),
			// "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(ccEmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip");
			// mimeMessageHelper.addAttachment(name + ".pdf", source);
			mailSender.send(mimeMessage);

			log.info("Mail send to the team hr Successfully" + name);
		} catch (MessagingException e) {
			log.info("Error", e.getMessage());
		}
	}

	@Async
	@Override
	public void sendEmailForTimeSheet(ByteArrayOutputStream baos, String name, String gmail, String date) {
		String massage = Util.TIME_SHEET_MSG.replace("[Name]", name).replace("[Company Name]", "AlphaDot Technologies")
				.replace("[Your Name]", "Team HR").replace("[date-range]", date);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(gmail);
			mimeMessageHelper.setCc(ccEmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Weekly Time Sheet Report");
			mimeMessageHelper.addAttachment(name + ".xlsx", source);

//			mailSender.send(mimeMessage);
			List<IsEmailSend> isEmailSendList = isEmailSendRepo.findAll();
			if (!isEmailSendList.isEmpty() && Boolean.TRUE.equals(isEmailSendList.get(0).getEmailSendStatus())) {
//				restTemplate.postForEntity(url, request, String.class);
				mailSender.send(mimeMessage);
				log.info("Mail send Successfully to {} ", gmail);

			} else {
				log.info("Email sending is disabled, as emailSendStatus:" + isEmailSendList.get(0).getEmailSendStatus()
						+ " is  or the list is empty.");
			}

			log.info("Mail send Successfully to {} ", gmail);
		} catch (MessagingException e) {
			log.info("Error");

		}
	}

	@Async
	@Override
	public void sendEmail(Map<ByteArrayOutputStream, String> baos, String name, String gmail, String monthYear) {
		String massage = Util.msg.replace("[Name]", name).replace("[Your Name]", "AlphaDot Technologies")
				.replace("[Month, Year]", monthYear);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;

		try {

			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(gmail);
			mimeMessageHelper.setText(massage);
			mimeMessageHelper.setSubject("Salary Slip" + "-" + monthYear);

			baos.entrySet().forEach(m -> {
				DataSource source = new ByteArrayDataSource(m.getKey().toByteArray(), "application/octet-stream");
				try {
					mimeMessageHelper.addAttachment(m.getValue() + ".pdf", source);
				} catch (MessagingException e) {
					log.error("Error getting while send mail", e.getMessage());
					e.printStackTrace();
				}
			});
			mailSender.send(mimeMessage);

			log.info("Mail send Successfully");
		} catch (MessagingException e) {
			log.info("Error");

		}
	}

	@Override
	public void sendEmail(OnLeaveRequestCancelEvent event) throws MessagingException, TemplateException, IOException {
		LeaveRequestModel leaveRequestModel = event.getLeaveRequestModel();

		// Ensure that `leavedate` is initialized if using lazy loading
		initializeLeaveRequestModel(leaveRequestModel);

		String emailCancelUrl = event.getRedirectUrl().toUriString();
		sendEmail(event, emailCancelUrl, leaveRequestModel);
	}

	private void initializeLeaveRequestModel(LeaveRequestModel leaveRequestModel) {
		// This method ensures that the collection is initialized
		if (leaveRequestModel != null) {
			// Access the collection to initialize it
			leaveRequestModel.getLeavedate().size();
		}
	}

	private String sendEmail(OnLeaveRequestCancelEvent event, String emailCancelUrl, LeaveRequestModel lrm)
			throws MessagingException, IOException, TemplateException {
		Mail mail = new Mail();
		mail.setSubject("Leave Cancel Request");

		Integer empID = lrm.getEmpid();
		Optional<User> user = userRepo.findById(empID);
		if (!user.isPresent()) {
			throw new RuntimeException("User not found with ID: " + empID);
		}

		// Get the email of the user (sender)
		String userEmail = user.get().getEmail();
		mail.setFrom(userEmail);

		String sql = "select * from av_schema.priortime_email";
		List<Map<String, Object>> priortimeData = dataExtractor.extractDataFromTable(sql);

		for (Map<String, Object> priortime : priortimeData) {
			String recipientEmail = String.valueOf(priortime.get("email_id"));
			String recipientName = String.valueOf(priortime.get("name")); // Adjust according to your data structure
			String token = auth.tokenGanreate(recipientEmail);

			mail.setTo(recipientEmail);
			mail.getModel().put("leaveCancelLink", emailCancelUrl + "?Authorization=" + token);
			mail.getModel().put("Name", user.get().getFirstName() + " " + user.get().getLastName()); // Set recipient's
																										// name here
			mail.getModel().put("LeaveType", lrm.getLeaveType());
			mail.getModel().put("LeaveDates", String.join(", ", lrm.getLeavedate())); // Join list into a single string
			mail.getModel().put("CancelReason", lrm.getCancelReason());

			try {
				templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
				Template template = templateConfiguration.getTemplate("leave-cancellation.ftl");
				String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
				mail.setContent(mailContent);

				String url = utilityServiceUrl + "/emails/send";
				LOGGER.info("Sending email to: " + recipientEmail);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON); // Correctly set Content-Type
				HttpEntity<Mail> request = new HttpEntity<>(mail, headers);
				restTemplate.postForEntity(url, request, String.class);
			} catch (TemplateException | IOException e) {
				throw new MessagingException("Failed to process email template", e);
			}
		}

		return "Mail Sent Successfully for Leave Cancellation";
	}

	@Override
	public void sendLeaveCancelEmail(OnLeaveCancelEvent onLeaveCancelEvent) {
		log.info("sendLeaveCancelEmail started");
		// Extract recipient address from leaveInfo
		String recipientAddress = onLeaveCancelEvent.getLeaveInfo().map(LeaveRequestModel::getEmail).orElse(null);
		// or provide a default address if appropriate
		if (recipientAddress == null) {
			log.error("Recipient address is null, cannot send email");
			throw new MailSendException("Recipient address is null");
		}
		try {
			sendleaveCancelResponseEmail(onLeaveCancelEvent, recipientAddress);
		} catch (IOException | TemplateException | MessagingException e) {
			log.error("Failed to send email", e);
			throw new MailSendException("Failed to send email to " + recipientAddress, e);
		}
	}

	private void sendleaveCancelResponseEmail(OnLeaveCancelEvent onLeaveCancelEvent, String recipientAddress)
			throws MessagingException, IOException, TemplateException {
		LeaveRequestModel leaveInfo = onLeaveCancelEvent.getLeaveInfo()
				.orElseThrow(() -> new IllegalArgumentException("Leave info is missing"));
		Mail mail = new Mail();
		mail.setSubject("Leave Cancel Request Status");
		mail.setFrom(mailFrom);
		mail.setTo(recipientAddress); // Ensure recipient address is correctly set
		mail.setModel(new HashMap<>()); // Initialize model
		mail.getModel().put("Message", leaveInfo.getMessage() != null ? leaveInfo.getMessage() : "No message provided");
		mail.getModel().put("Name", leaveInfo.getName() != null ? leaveInfo.getName() : "N/A");
//		mail.getModel().put("LeaveBalance", leaveInfo.getLeaveBalance() != null ? leaveInfo.getLeaveBalance().toString() : "N/A");
		mail.getModel().put("LeaveType", leaveInfo.getLeaveType() != null ? leaveInfo.getLeaveType() : "N/A");
		mail.getModel().put("CancelReason", leaveInfo.getCancelReason() != null ? leaveInfo.getCancelReason() : "N/A");
		mail.getModel().put("LeaveDates",
				leaveInfo.getLeavedate() != null ? String.join(", ", leaveInfo.getLeavedate()) : "N/A");
		mail.getModel().put("Status", leaveInfo.getStatus() != null ? leaveInfo.getStatus() : "N/A");
		try {
			templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
			Template template = templateConfiguration.getTemplate("leave_cancellation_status.ftl");
			String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
			mail.setContent(mailContent);
			String url = utilityServiceUrl + "/emails/send";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON); // Correctly set Content-Type
			HttpEntity<Mail> request = new HttpEntity<>(mail, headers);
			restTemplate.postForEntity(url, request, String.class);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (TemplateException | IOException e) {
			log.error("Failed to process email template", e);
			throw new MessagingException("Failed to process email template", e);
		} catch (Exception e) {
			log.error("Failed to send email", e);
			throw new MessagingException("Failed to send email", e);
		}
	}

	@Override
	public void sendEmailVerification(OnCompOffDetailsSavedEvent onCompOffDetailsSavedEvent)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			MessagingException {
		Mail mail = new Mail();
		mail.setSubject("Compensation off Request");
		Optional<User> user = userRepo.findById(onCompOffDetailsSavedEvent.getCompOff().getEmpId());
		String userEmail = user.get().getEmail();
		mail.setFrom(userEmail);
		String sql = "select * from av_schema.priortime_email";
		List<String> emailArray = new ArrayList<>();
		List<Map<String, Object>> priortimeData = dataExtractor.extractDataFromTable(sql);
		for (Map<String, Object> priortime : priortimeData) {
			String email = String.valueOf(priortime.get("email_id"));
			String token = auth.tokenGanreate(email);
			// mail.setTo("sunalisingh.adt@gmail.com");
			mail.setTo(email);
			mail.getModel().put("compOffApprovalLink",
					onCompOffDetailsSavedEvent.getRedirectUrl1().toUriString() + "?Authorization=" + token);
			mail.getModel().put("compOffRejectionLink",
					onCompOffDetailsSavedEvent.getRedirectUrl2().toUriString() + "?Authorization=" + token);
			mail.getModel().put("CheckInTime", onCompOffDetailsSavedEvent.getCompOff().getCheckin().toString());
			mail.getModel().put("CheckOutTime", onCompOffDetailsSavedEvent.getCompOff().getCheckout().toString());
			// mail.getModel().put("workingHours",
			// onUserRegistrationCompleteEvent.getCompOff().getw);
			mail.getModel().put("EmpId", String.valueOf(onCompOffDetailsSavedEvent.getCompOff().getEmpId()));

			mail.getModel().put("Name", user.get().getFirstName().concat(user.get().getLastName()));
			mail.getModel().put("Date", onCompOffDetailsSavedEvent.getCompOff().getDate().toString());
			templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
			Template template = templateConfiguration.getTemplate("compoff_email_verification.ftl");
			String mailContent = null;
			try {
				mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
			} catch (IOException | TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mail.setContent(mailContent);

			// Send email
			String url = utilityServiceUrl + "/emails/send";
			HttpEntity<Mail> request = new HttpEntity<>(mail);
			restTemplate.postForEntity(url, request, String.class);
			// send(mail);
		}

	}

	@Override
	public void sendEmpCompOffApprovedOrRejectedEmail(OnEmpCompOffApproveOrRejectEvent event)
			throws TemplateException, IOException {
		log.info("Payroll: sendEmpCompOffApprovedOrRejectedEmail : Sending comp-off {} email for employee ID: {}",
				event.getActionStatus(), event.getCompOff().getEmpId());
		try {
			CompOff compOff = event.getCompOff();

			Optional<User> user = userRepo.findByEmployeeId(compOff.getEmpId());
			if (!user.isPresent()) {
				log.warn("User not found for employee ID: {}", compOff.getEmpId());
				return;
			}

			User userEntity = user.get();
			String userEmail = userEntity.getEmail();
			String employeeName = userEntity.getFirstName() + " " + userEntity.getLastName();
			String subject = "Comp-Off Request " + event.getActionStatus();
			String message = "Your comp-off request has been " + event.getActionStatus()
					+ ". Below are the comp-off request details.";
//			String token = auth.tokenGanreate(email);
			Mail mail = new Mail();
			mail.setSubject(subject);
			mail.setTo(userEmail);

			// *** Get recipient email and generate token ***
//			String sql = "SELECT email_id FROM av_schema.priortime_email where designation='CEO'";
//			List<Map<String, Object>> compOffData = dataExtractor.extractDataFromTable(sql);
//			String token = null;
//			for (Map<String, Object> data : compOffData) {
//				String email = String.valueOf(data.get("email_id"));
//				token = auth.tokenGanreate(email);
//			}

			Map<String, Object> model = new HashMap<>();
			model.put("Message", message);
			model.put("Email", userEmail);
			model.put("employeeName", employeeName);
			model.put("actionStatus", event.getActionStatus());
			model.put("action", "Comp-Off Request");
			model.put("CheckIn", compOff.getCheckin());
			model.put("CheckOut", compOff.getCheckout());
			model.put("Date", compOff.getDate());
			model.put("CompOffStatus", event.getActionStatus());

			templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
			Template template = templateConfiguration.getTemplate("compOff_request_confirmed.ftl");
			String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			mail.setContent(mailContent);

			String url = utilityServiceUrl + "/emails/send";
			HttpEntity<Mail> request = new HttpEntity<>(mail);
			restTemplate.postForEntity(url, request, String.class);

		} catch (Exception e) {
			log.error("sendEmpCompOffApprovedOrRejectedEmail : Error while sending CompOffStatus email: {}",
					e.getMessage());
			e.printStackTrace();
		}
	}
}
