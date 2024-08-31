package com.adt.payroll.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	private final Configuration templateConfiguration;

	@Value("${app.velocity.templates.location}")
	private String basePackagePath;

	@Autowired
	public EmailService(JavaMailSender javaMailSender, Configuration templateConfiguration ) {
		this.javaMailSender = javaMailSender;
		this.templateConfiguration = templateConfiguration;
	}

	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr)
			throws IOException, TemplateException, MessagingException{

		Mail mail =  new Mail();
		mail.setSubject("Leave Request");
		mail.setFrom("mukeshchandalwar.adt@gmail.com");
		mail.setTo("dhananjaybobde.adt@gmail.com");
		mail.getModel().put("leaveApprovalLink", Url);
		mail.getModel().put("leaveRejectionLink", Url1);
		mail.getModel().put("LeaveId", event.getLeaveRequestModel().getLeaveid().toString() );
		mail.getModel().put("EmpId", event.getLeaveRequestModel().getEmpid().toString());
		mail.getModel().put("Name", event.getLeaveRequestModel().getName());
		mail.getModel().put("LeaveType", event.getLeaveRequestModel().getLeaveType());
		mail.getModel().put("Reason", event.getLeaveRequestModel().getLeaveReason());
		mail.getModel().put("LeaveDates", event.getLeaveRequestModel().getLeavedate().toString());
		mail.getModel().put("Status", event.getLeaveRequestModel().getStatus());

		templateConfiguration.setClassForTemplateLoading(getClass(), basePackagePath);
		Template template = templateConfiguration.getTemplate("leave_status_change.ftl");
		String mailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, mail.getModel());
		mail.setContent(mailContent);
		send(mail);

		return "Mail Sent Successfully";


	}

	public String send(Mail mail) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		helper.setTo(mail.getTo());
		helper.setText(mail.getContent(), true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());
		javaMailSender.send(message);

		return "Mail Sent Successfully";
	}




	public String sendEmail(LeaveRequestModel lr, String recipientEmail) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

		helper.setTo("lilesh09sharma.adt@gmail.com"); // Use the dynamic recipient email
		helper.setText(lr.getMessage(), true); // Use the dynamic email body, assume message contains email body
		helper.setSubject("Leave Request Cancellation"); // Set email subject
		helper.setFrom("teamhr.adt@gmail.com"); // Your email address as the sender

		javaMailSender.send(message);

		return "Mail Sent Successfully";
	}

	public void sendEmail(String recipientEmail, String subject, String text) {
	}

	// Overloaded method to send email using LeaveRequestModel object directly
//	public String sendEmail(LeaveRequestModel lr) throws MessagingException {
//		return sendEmail(lr, lr.getEmail()); // Call the method using the email from LeaveRequestModel
//	}

}
