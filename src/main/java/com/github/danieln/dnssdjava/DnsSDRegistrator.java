/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.UnknownHostException;


/**
 * A DnsSDRegistrator object provides methods for registering services.
 * @author Daniel Nilsson
 */
public interface DnsSDRegistrator {

	/**
	 * Make a service name for a service in this DnsSDRegistrators domain.
	 * @param name the service name.
	 * @param type the service type.
	 * @return a service name.
	 */
	ServiceName makeServiceName(String name, ServiceType type);

	/**
	 * Convenience method for getting the fully qualified name of the local host.
	 * The host name is often used for passing to {@link ServiceData#setHost(String)}.
	 * This method seems to work better than using
	 * <code>InetAddress.getLocalHost().getCanonicalHostName()</code>
	 * @return the fully qualified host name.
	 * @throws UnknownHostException if the host name cannot be found.
	 */
	String getLocalHostName() throws UnknownHostException;
	
	/**
	 * Add a new service to DNS-SD.
	 * If the service name is already taken this method will not update the service data,
	 * but return false to indicate the collision.
	 * @param serviceData the service to register.
	 * @return true if the service was registered, false if the service name was already registered.
	 * @throws DnsSDException if the service couldn't be registered due to some error.
	 */
	boolean registerService(ServiceData serviceData) throws DnsSDException;

	/**
	 * Remove a service from DNS-SD.
	 * @param serviceName the name of the service to remove.
	 * @return true if the service was removed, false if no service was found.
	 * @throws DnsSDException if the service couldn't be unregistered due to some error.
	 */
	boolean unregisterService(ServiceName serviceName) throws DnsSDException;

}