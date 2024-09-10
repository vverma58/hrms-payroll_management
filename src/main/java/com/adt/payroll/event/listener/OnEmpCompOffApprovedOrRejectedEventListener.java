package com.adt.payroll.event.listener;

import com.adt.payroll.event.OnEmpCompOffApproveOrRejectEvent;
import com.adt.payroll.model.CompOff;
import com.adt.payroll.service.CommonEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnEmpCompOffApprovedOrRejectedEventListener
		implements ApplicationListener<OnEmpCompOffApproveOrRejectEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private final CommonEmailService emailService;

	@Autowired
	public OnEmpCompOffApprovedOrRejectedEventListener(CommonEmailService emailService) {
		this.emailService = emailService;
	}

	@Override
	@Async
	public void onApplicationEvent(OnEmpCompOffApproveOrRejectEvent event) {
		try {
			CompOff compOff = event.getCompOff();
//			CompOff compOff=event.getActionStatus();
//			if (compOff.getStatus().equalsIgnoreCase("Approved") || compOff.getStatus().equalsIgnoreCase("Rejected")) 
			if (event.getActionStatus() != null && event.getActionStatus() != null) {
				LOGGER.info("Handling comp-off {} event for employee ID: {}", event.getActionStatus(),
						compOff.getEmpId());
				emailService.sendEmpCompOffApprovedOrRejectedEmail(event);
			} else {
				LOGGER.warn("Unhandled event type or invalid comp-off status: {}", compOff.getStatus());
			}
		} catch (Exception e) {
			LOGGER.error("Error handling comp-off event: {}", e.getMessage());
			e.printStackTrace();
		}
	}
}
