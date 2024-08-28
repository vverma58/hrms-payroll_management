package com.adt.payroll.model;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

public class OnLeaveRequestCancelEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private transient UriComponentsBuilder redirectUrl;
    private LeaveRequestModel leaveRequestModel;

    public OnLeaveRequestCancelEvent(UriComponentsBuilder redirectUrl, LeaveRequestModel leaveRequestModel) {
        super(leaveRequestModel);
        this.redirectUrl = redirectUrl;
        this.leaveRequestModel = leaveRequestModel;
    }

    public UriComponentsBuilder getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(UriComponentsBuilder redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public LeaveRequestModel getLeaveRequestModel() {
        return leaveRequestModel;
    }

    public void setLeaveRequestModel(LeaveRequestModel leaveRequestModel) {
        this.leaveRequestModel = leaveRequestModel;
    }
}
