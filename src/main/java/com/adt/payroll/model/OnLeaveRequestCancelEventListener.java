package com.adt.payroll.model;

import com.adt.payroll.service.CommonEmailService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OnLeaveRequestCancelEventListener implements ApplicationListener<OnLeaveRequestCancelEvent> {

    private CommonEmailService emailService;

    @Autowired
    public OnLeaveRequestCancelEventListener(CommonEmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnLeaveRequestCancelEvent event) {

        try {
            emailService.sendEmail(event);
        } catch (MessagingException | TemplateException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
