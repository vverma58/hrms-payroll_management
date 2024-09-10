package com.adt.payroll.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.model.CompOff;

import lombok.Data;

@Data
public class OnEmpCompOffApproveOrRejectEvent extends ApplicationEvent {

	private final CompOff compOff;
	private final String action;
	private final String actionStatus;

//	private UriComponentsBuilder approvedUrlBuilder;
//	private UriComponentsBuilder rejectedUrlBuilder;

	public OnEmpCompOffApproveOrRejectEvent(CompOff compOff, String action, String actionStatus) {
		super(compOff);
		this.compOff = compOff;
		this.action = action;
		this.actionStatus = actionStatus;
	}

//	   public OnEmpCompOffApproveOrRejectEvent(UriComponentsBuilder approvedUrlBuilder, UriComponentsBuilder rejectedUrlBuilder, CompOff compOff) {
//	        super();
//	        this.compOff = compOff;
//	        this.approvedUrlBuilder = approvedUrlBuilder;
//	        this.rejectedUrlBuilder = rejectedUrlBuilder;
//	    }

	public CompOff getCompOff() {
		return compOff;
	}

	public String getAction() {
		return action;
	}

	public String getActionStatus() {
		return actionStatus;
	}

//	public UriComponentsBuilder getApproveUrlBuilder() {
//		return approvedUrlBuilder;
//	}
//
//	public void setApproveUrlBuilder(UriComponentsBuilder approveUrlBuilder) {
//		this.approvedUrlBuilder = approveUrlBuilder;
//	}
//
//	public UriComponentsBuilder getRejectUrlBuilder() {
//		return rejectedUrlBuilder;
//	}
//
//	public void setRejectUrlBuilder(UriComponentsBuilder rejectUrlBuilder) {
//		this.rejectedUrlBuilder = rejectUrlBuilder;
//	}
}
