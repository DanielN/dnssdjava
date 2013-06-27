/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	private final List<String> subtypes;

	/**
	 * Create a new ServiceType.
	 * @param type the service type (eg. "_http").
	 * @param transport the transport protocol.
	 */
	public ServiceType(String type, Transport transport) {
		this.type = type;
		this.transport = transport;
		this.subtypes = Collections.emptyList();
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
	 * Create a new ServiceType with subtypes.
	 * For internal use only.
	 * @param baseType the base service type.
	 * @param subtypes the subtypes of the service type, if any.
	 */
	private ServiceType(ServiceType baseType, String ...subtypes) {
		this.type = baseType.type;
		this.transport = baseType.transport;
		this.subtypes = Collections.unmodifiableList(Arrays.asList(subtypes));
	}

	/**
	 * Create a subtype variant of this ServiceType.
	 * Any existing subtypes in this ServiceType is not passed on to the new ServiceType.
	 * A subtype of a ServiceType only provides additional filtering when browsing, it is still
	 * the same service type. In particular the subtype variant still {@link #equals(Object)}
	 * the base ServiceType and has the same {@link #hashCode()}.
	 * @param subtype the subtype.
	 * @return a new ServiceType based on this ServiceType but with the given subtype.
	 */
	public ServiceType withSubtype(String subtype)
	{
		return new ServiceType(this, subtype);
	}

	/**
	 * Create a variant of this ServiceType with multiple subtypes.
	 * Any existing subtypes in this ServiceType is not passed on to the new ServiceType.
	 * A subtype of a ServiceType only provides additional filtering when browsing, it is still
	 * the same service type. In particular the subtype variant still {@link #equals(Object)}
	 * the base ServiceType and has the same {@link #hashCode()}.
	 * @param subtypes the subtypes.
	 * @return a new ServiceType based on this ServiceType but with the given subtypes.
	 */
	public ServiceType withSubtypes(String ...subtypes)
	{
		return new ServiceType(this, subtypes);
	}

	/**
	 * Returns a ServiceType representing the base type of this ServiceType.
	 * If there are no subtypes then this ServiceType is returned
	 * else a new ServiceType is returned based on this but without the subtypes.
	 * @return the base ServiceType without subtypes.
	 */
	public ServiceType baseType()
	{
		if (subtypes.isEmpty()) {
			return this;
		} else {
			return new ServiceType(type, transport);
		}
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

	/**
	 * Get the list of subtypes.
	 * @return the list of subtypes.
	 */
	public List<String> getSubtypes() {
		return subtypes;
	}

	/**
	 * Returns a string representation of the ServiceType.
	 * Examples: "_http._tcp", "_ftp._tcp,_anon".
	 * @return a string of the format "{type}.{transport}[,{subtype}][,{subtype}][...]".
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append('.').append(transport);
		for (String subtype : subtypes) {
			sb.append(',').append(subtype);
		}
		return sb.toString();
	}

	/**
	 * Returns a ServiceType object representing the type specified in the String.
	 * The argument is expected to be in the format returned by {@link #toString()}.
	 * @param s the string to be parsed.
	 * @return a ServiceType representing the type specified by the argument.
	 * @throws IllegalArgumentException if the string cannot be parsed as a ServiceType.
	 */
	public static ServiceType valueOf(String s) {
		int i = s.indexOf(',');
		String domain = (i < 0) ? s : s.substring(0, i);
		String sublist = (i < 0) ? null : s.substring(i+1);
		i = domain.indexOf('.');
		if (i < 0) {
			throw new IllegalArgumentException("No '.' in service type: " + s);
		}
		String type = domain.substring(0, i);
		String transport = domain.substring(i+1);
		ServiceType res = new ServiceType(type, transport);
		if (sublist != null) {
			String[] subs = sublist.split(",");
			for (String sub : subs) {
				if (sub.isEmpty()) {
					throw new IllegalArgumentException("Zero length subtype is not allowed: " + s);
				}
			}
			res = res.withSubtypes(subs);
		}
		return res;
	}

	/**
	 * Get the DNS-SD subdomain that represents this type (excluding any subtypes).
	 * For internal use only.
	 * @return A string of the form "{type}.{transport}".
	 */
	String toDnsString() {
		return type + "." + transport;
	}
	
	/**
	 * Get the DNS-SD subdomains that represent this type, one for each subtype.
	 * For internal use only.
	 * @return A list of strings of the form "{subtype}._sub.{type}.{transport}".
	 */
	List<String> toDnsStringsWithSubtype() {
		List<String> list = new ArrayList<String>(subtypes.size());
		for (String subtype : subtypes) {
			list.add(subtype + "._sub." + type + "." + transport);
		}
		return list;
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
