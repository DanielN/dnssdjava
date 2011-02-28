/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.UnknownHostException;


/**
 *
 * @author daniel.nilsson
 */
public interface DnsSDRegistrator {

	ServiceName makeServiceName(String name, ServiceType type);

	String getLocalHostName() throws UnknownHostException;
	
	boolean registerService(ServiceData serviceData) throws DnsSDException;

	boolean unregisterService(ServiceName serviceName) throws DnsSDException;

}