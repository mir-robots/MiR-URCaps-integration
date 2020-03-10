package com.mir.urcap.MiRintegration.common;

import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidator;

public class RegisterValidator implements InputValidator<Integer> {
	
	private static final Integer MIN = 1;
	private static final Integer MAX = 200;

	@Override
	public boolean isValid(Integer value) {
		if (value >= MIN && value <= MAX) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String getMessage(Integer value) {
		return "Register must be between " + MIN + " and " + MAX;
	}

}
