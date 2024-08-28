package com.adt.payroll.event.listener;

import com.adt.payroll.event.OnLeaveAcceptOrRejectEvent;
import com.adt.payroll.event.OnLeaveCancelEvent;
import com.adt.payroll.service.CommonEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnLeaveCancelEventListener implements ApplicationListener<OnLeaveCancelEvent> {
    CommonEmailService emailService;

    @Autowired
    public OnLeaveCancelEventListener(CommonEmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnLeaveCancelEvent onLeaveCancelEvent) {
//		sendAccountChangeEmailRejected(onPriortimeApprovalEvent);
        emailService.sendLeaveCancelEmail(onLeaveCancelEvent);
    }
}
