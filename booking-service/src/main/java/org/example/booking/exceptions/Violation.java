package org.example.booking.exceptions;

public class Violation {

	private final String fieldName;

	private final String message;

	public Violation(String fieldName, String message) {
		super();
		this.fieldName = fieldName;
		this.message = message;
	}

}
