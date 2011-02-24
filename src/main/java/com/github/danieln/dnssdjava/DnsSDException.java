package com.github.danieln.dnssdjava;


/**
 * 
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
