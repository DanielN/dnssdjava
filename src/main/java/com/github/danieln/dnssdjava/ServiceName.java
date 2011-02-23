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
 *
 * @author daniel.nilsson
 */
public class ServiceName {

	private static final Charset NET_UNICODE = Charset.forName("UTF-8");

	private final String name;
	private final ServiceType type;
	private final String domain;

	public ServiceName(String name, ServiceType type, String domain) {
		this.name = name;
		this.type = type;
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public ServiceType getType() {
		return type;
	}

	public String getDomain() {
		return domain;
	}

	@Override
	public String toString() {
		return name + '.' + type + '.' + domain;
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

	private static String decodeName(byte[] label) {
		// First byte is length
		return new String(label, 1, label.length - 1, NET_UNICODE);
	}

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

}
