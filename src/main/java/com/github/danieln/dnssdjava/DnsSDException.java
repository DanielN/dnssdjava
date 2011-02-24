/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;


/**
 * Thrown when a DNS-SD operation fails.
 * @author Daniel Nilsson
 */
public class DnsSDException extends Exception {

	private static final long serialVersionUID = 1L;

	public DnsSDException() {
	}

	public DnsSDException(String message) {
		super(message);
	}

	public DnsSDException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
