package com.adt.payroll.msg;

import org.springframework.http.HttpStatus;

public class HttpStatusMapper {

	public static HttpStatus mapToHttpStatus(String status) {
		switch (status) {

		case "Success":
			return HttpStatus.OK;

		case "AlreadyAssociated":
			return HttpStatus.OK;

		case "NotSaved":
			return HttpStatus.BAD_REQUEST;

		case "NotUpdated":
			return HttpStatus.BAD_REQUEST;

		case "AlreadyExist":
			return HttpStatus.CONFLICT;

		case "NotFound":
			return HttpStatus.NOT_FOUND;

		case "Failed":
			return HttpStatus.INTERNAL_SERVER_ERROR;

		default:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
}
