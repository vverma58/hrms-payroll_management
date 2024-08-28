package com.adt.payroll.event;

import com.adt.payroll.model.LeaveRequestModel;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

public class OnLeaveCancelEvent extends ApplicationEvent {
    private Optional<LeaveRequestModel> leaveInfo;

    public Optional<LeaveRequestModel> getLeaveInfo() {
        return leaveInfo;
    }

    public void setLeaveInfo(Optional<LeaveRequestModel> leaveInfo) {
        this.leaveInfo = leaveInfo;
    }

    public OnLeaveCancelEvent(Optional<LeaveRequestModel> leaveInfo) {
        super(leaveInfo);
        this.leaveInfo = leaveInfo;
    }
}
