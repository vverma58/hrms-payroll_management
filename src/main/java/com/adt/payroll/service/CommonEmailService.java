package com.adt.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.adt.payroll.event.*;
import com.adt.payroll.model.OnLeaveRequestCancelEvent;
import jakarta.mail.MessagingException;

import com.adt.payroll.model.LeaveRequestModel;
import com.adt.payroll.model.Mail;
import com.adt.payroll.model.OnLeaveRequestSaveEvent;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.TemplateException;

public interface CommonEmailService {

	// START:- For Prior Time
	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event);

	public void sendEmailVerification(OnPriorTimeDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException;

	public void sendAccountChangeEmail(OnPriorTimeAcceptOrRejectEvent event, String action, String actionStatus,
			String to) throws IOException, TemplateException, MessagingException;

	public void send(Mail mail) throws MessagingException, UnsupportedEncodingException;

	public void sendAccountChangeEmailRejected(OnPriorTimeAcceptOrRejectEvent event);

	public void sendLeaveAcceptAndRejectedEmail(OnLeaveAcceptOrRejectEvent event);

	// END:- For Prior Time

	// *** Send email after generating PaySlip ***
	public void sendEmail(ByteArrayOutputStream baos, String name, String gmail, String monthYear);

	// *** START:- Send Email for Leave Request
	public String sendEmail(OnLeaveRequestSaveEvent event, String Url, String Url1, LeaveRequestModel lr)
			throws IOException, TemplateException, MessagingException;

	public void sendEmail(OnLeaveRequestSaveEvent event);
	// *** END:- Send Email for Leave Request

	public void sendEmployeeExpenseVerification(OnEmployeeExpenseDetailsSavedEvent event, String emailVerificationUrl1,
			String emailVerificationUrl2, String to) throws IOException, TemplateException, MessagingException;

	public void sendEmployeeExpenseApprovalEmail(OnEmployeeExpenseAcceptOrRejectEvent event, String action,
			String actionStatus, String to) throws IOException, TemplateException, MessagingException;

	public void sendAccountChangeEmailApproved(OnEmployeeExpenseAcceptOrRejectEvent event)
			throws IOException, TemplateException, MessagingException;

	public void sendEmail(String name);

	public void sendEmail(String name, String msg);

	void sendEmailForTimeSheet(ByteArrayOutputStream baos, String name, String gmail, String date);

	public void sendEmail(Map<ByteArrayOutputStream, String> baos, String name, String gmail, String monthYear);

	// public void sendEmail(LeaveRequestModel leaveRequestModel, String
	// recipientEmail) throws MessagingException;

	public void sendEmail(OnLeaveRequestCancelEvent event) throws MessagingException, TemplateException, IOException;

	void sendLeaveCancelEmail(OnLeaveCancelEvent onLeaveCancelEvent);

	void sendEmailVerification(OnCompOffDetailsSavedEvent onUserRegistrationCompleteEvent)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			MessagingException;

	public void sendEmpCompOffApprovedOrRejectedEmail(OnEmpCompOffApproveOrRejectEvent event)
			throws TemplateException, IOException;
}
