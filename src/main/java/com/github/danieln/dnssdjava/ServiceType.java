/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

/**
 *
 * @author daniel.nilsson
 */
public class ServiceType {

	public enum Transport {
		TCP("_tcp"),
		UDP("_udp");

		private final String label;

		private Transport(String label) {
			this.label = label;
		}

		String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}

		static Transport fromLabel(String label) {
			for (Transport t : values()) {
				if (t.getLabel().equalsIgnoreCase(label)) {
					return t;
				}
			}
			throw new IllegalArgumentException("Not a valid transport label: " + label);
		}
	}

	private final String type;
	private final Transport transport;

	public ServiceType(String type, Transport transport) {
		this.type = type;
		this.transport = transport;
	}

	ServiceType(String type, String transport) {
		this(type, Transport.fromLabel(transport));
	}

	public String getType() {
		return type;
	}

	public Transport getTransport() {
		return transport;
	}

	@Override
	public String toString() {
		return type + "." + transport;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ServiceType other = (ServiceType) obj;
		if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
			return false;
		}
		if (this.transport != other.transport) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
		hash = 29 * hash + (this.transport != null ? this.transport.hashCode() : 0);
		return hash;
	}

}
