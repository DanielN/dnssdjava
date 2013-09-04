/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.io.IOException;
import java.nio.charset.Charset;
import org.xbill.DNS.Name;

/**
 * A unique identifier for a service instance.
 * A service name consists of the triple domain, service type and name.
 * The domain is the fully qualified domain where the service is registered.
 * The service type specified the protocol to use when accessing the service.
 * The name identifies this particular instance of the service. Instance names
 * should be human readable and can contain spaces, punctuation and international
 * characters.
 * <p>
 * Instances of the class are immutable.
 * @author Daniel Nilsson
 */
public class ServiceName {

	private static final Charset NET_UNICODE = Charset.forName("UTF-8");

	private final String name;
	private final ServiceType type;
	private final String domain;

	/**
	 * Create a new ServiceName.
	 * @param name the name of the service.
	 * @param type the type of service.
	 * @param domain the fully qualified domain name.
	 */
	public ServiceName(String name, ServiceType type, String domain) {
		this.name = name;
		this.type = type;
		this.domain = domain;
	}

	/**
	 * Get the service name.
	 * @return the service name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the service type.
	 * @return the service type.
	 */
	public ServiceType getType() {
		return type;
	}

	/**
	 * Get the fully qualified domain name.
	 * @return the domain name.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Returns a ServiceName object representing the service specified in the String.
	 * The argument is expected to be in the format returned by {@link #toString()}.
	 * @param s the string to be parsed.
	 * @return a ServiceName representing the service specified by the argument.
	 * @throws IllegalArgumentException if the string cannot be parsed as a ServiceName.
	 */
	public static ServiceName valueOf(String s) {
		int i = indexOfNonEscaped(s, '.');
		if (i < 0) {
			throw new IllegalArgumentException("No '.' in service name: " + s);
		}
		String name = unescape(s.substring(0, i));
		int j = s.indexOf('.', i+1);
		if (j < 0) {
			throw new IllegalArgumentException("No '.' in service type: " + s);
		}
		j = s.indexOf('.', j+1);
		if (j < 0) {
			throw new IllegalArgumentException("No '.' after service type: " + s);
		}
		ServiceType type = ServiceType.valueOf(s.substring(i+1, j));
		i = s.indexOf(',', j+1);
		String domain = (i < 0) ? s.substring(j+1) : s.substring(j+1, i);
		if (i >= 0) {
			String sublist = s.substring(i+1);
			String[] subs = sublist.split(",");
			for (String sub : subs) {
				if (sub.isEmpty()) {
					throw new IllegalArgumentException("Zero length subtype is not allowed: " + s);
				}
			}
			type = type.withSubtypes(subs);
		}
		return new ServiceName(name, type, domain);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(escape(name)).append('.').append(type.toDnsString()).append('.').append(domain);
		for (String subtype : type.getSubtypes()) {
			sb.append(',').append(subtype);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ServiceName other = (ServiceName) obj;
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
			return false;
		}
		if ((this.domain == null) ? (other.domain != null) : !this.domain.equals(other.domain)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
		hash = 89 * hash + (this.domain != null ? this.domain.hashCode() : 0);
		return hash;
	}

	/**
	 * Convert to a dnsjava {@link Name}.
	 * This is an internal helper method.
	 * @return the ServiceName as a Name.
	 */
	Name toDnsName() {
		try {
			Name dnsname = Name.fromString(domain);
			dnsname = Name.fromString(type.getTransport().getLabel(), dnsname);
			dnsname = Name.fromString(type.getType(), dnsname);
			dnsname = Name.concatenate(new Name(encodeName(name)).relativize(Name.root), dnsname);
			return dnsname;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Invalid DNS name", ex);
		}
	}

	/**
	 * Make a new ServiceName from a dnsjava {@link Name}.
	 * @param dnsname the Name to convert.
	 * @return the Name as a ServiceName.
	 */
	static ServiceName fromDnsName(Name dnsname) {
		if (dnsname.labels() < 4) {
			throw new IllegalArgumentException("Too few labels in service name: " + dnsname);
		}
		String name = decodeName(dnsname.getLabel(0));
		String type = dnsname.getLabelString(1);
		String transport = dnsname.getLabelString(2);
		String domain = new Name(dnsname, 3).toString();
		return new ServiceName(name, new ServiceType(type, transport), domain);
	}

	/**
	 * Decode a raw DNS label into a string.
	 * The methods in dnsjava don't understand UTF-8 and escapes some characters,
	 * we don't want that here.
	 * @param label the raw label data.
	 * @return the decoded string.
	 */
	private static String decodeName(byte[] label) {
		// First byte is length
		return new String(label, 1, label.length - 1, NET_UNICODE);
	}

	/**
	 * Encode a string into a raw DNS label.
	 * The methods in dnsjava don't understand UTF-8 and escapes some characters,
	 * we don't want that here.
	 * @param s the string to encode.
	 * @return the raw DNS label.
	 */
	private byte[] encodeName(String s) {
		byte[] tmp = s.getBytes(NET_UNICODE);
		if (tmp.length > 63) {
			throw new IllegalArgumentException("Name too long: " + s);
		}
		byte[] bytes = new byte[tmp.length + 2];
		bytes[0] = (byte) tmp.length;
		System.arraycopy(tmp, 0, bytes, 1, tmp.length);
		// implicit: bytes[tmp.length + 1] = 0;
		return bytes;
	}

	/**
	 * Escape a service name according to RFC6763 chapter 4.3.
	 * @param name the name to escape.
	 * @return the name with '.' and '\' escaped.
	 */
	private static String escape(String name) {
		return name.replaceAll("\\\\|\\.", "\\\\$0");		// Replace "\" with "\\" and "." with "\."
	}

	/**
	 * Undo escaping of a service name.
	 * @see #escape(String)
	 * @param name the escaped name.
	 * @return the name with escapes removed.
	 */
	private static String unescape(String name) {
		return name.replaceAll("\\\\(.)", "$1");		// Replace "\x" with "x" for any x
	}

	/**
	 * Find the first non-escaped occurrence of a character in a string.
	 * @see String#indexOf(int)
	 * @param string the string to look through.
	 * @param ch the character to find.
	 * @return the index of the first occurrence, or -1 if it can't be found. 
	 */
	private static int indexOfNonEscaped(String string, char ch) {
		for (int i = 0; i < string.length(); i++) {
			int c = string.charAt(i);
			if (c == '\\') {
				i++;
			} else if (c == ch) {
				return i;
			}
		}
		return -1;
	}

}
