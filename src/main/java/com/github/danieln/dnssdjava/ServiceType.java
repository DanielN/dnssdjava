/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

/**
 * Identifiers for service types.
 * A DNS-SD service type consists of the application protocol name
 * prepended with an underscore and the transport protocol (TCP or UDP).
 * @author Daniel Nilsson
 */
public class ServiceType {

	/**
	 * The transport protocol.
	 */
	public enum Transport {
		TCP("_tcp"),
		UDP("_udp");

		private final String label;

		private Transport(String label) {
			this.label = label;
		}

		/**
		 * Get the DNS label.
		 * @return the DNS label.
		 */
		String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}

		/**
		 * Get the Transport corresponding to a DNS label.
		 * @param label the DNS label.
		 * @return the corresponding Transport constant.
		 */
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

	/**
	 * Create a new ServiceType.
	 * @param type the service type (eg. "_http");
	 * @param transport the transport protocol.
	 */
	public ServiceType(String type, Transport transport) {
		this.type = type;
		this.transport = transport;
	}

	/**
	 * Create a new ServiceType.
	 * For internal use only.
	 * @param type the service type.
	 * @param transport the transport DNS label.
	 */
	ServiceType(String type, String transport) {
		this(type, Transport.fromLabel(transport));
	}

	/**
	 * Get the service type.
	 * @return the service type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the transport protocol.
	 * @return the transport protocol.
	 */
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
