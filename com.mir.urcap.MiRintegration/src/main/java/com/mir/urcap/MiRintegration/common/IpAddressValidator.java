package com.mir.urcap.MiRintegration.common;

import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidator;

public class IpAddressValidator implements InputValidator<String> {

	@Override
	public boolean isValid(String value) {
		return !value.equals("0.0.0.0");
	}

	@Override
	public String getMessage(String value) {
		return value + " is not a valid IP";
	}
	
}
