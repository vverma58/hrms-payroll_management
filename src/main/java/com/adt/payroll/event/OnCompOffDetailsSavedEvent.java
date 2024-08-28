package com.adt.payroll.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

import com.adt.payroll.model.CompOff;
import com.adt.payroll.model.Priortime;

public class OnCompOffDetailsSavedEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient UriComponentsBuilder redirectUrl1;
	private transient UriComponentsBuilder redirectUrl2;

	private CompOff compOff;

	public OnCompOffDetailsSavedEvent(CompOff compOff, UriComponentsBuilder redirectUrl1,UriComponentsBuilder redirectUrl2) {
		super(compOff);
		this.compOff = compOff;
		this.redirectUrl1 = redirectUrl1;
		this.redirectUrl2 = redirectUrl2;
	}
	

	public UriComponentsBuilder getRedirectUrl1() {
		return redirectUrl1;
	}


	public void setRedirectUrl1(UriComponentsBuilder redirectUrl1) {
		this.redirectUrl1 = redirectUrl1;
	}


	public UriComponentsBuilder getRedirectUrl2() {
		return redirectUrl2;
	}


	public void setRedirectUrl2(UriComponentsBuilder redirectUrl2) {
		this.redirectUrl2 = redirectUrl2;
	}


	public CompOff getCompOff() {
		return compOff;
	}


	public void setCompOff(CompOff compOff) {
		this.compOff = compOff;
	}


	

}
