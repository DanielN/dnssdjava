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
interface DnsSDRegistrator {

	ServiceName makeServiceName(String name, ServiceType type);

	boolean registerService(ServiceData serviceData) throws DnsSDException;

	void unregisterService(ServiceName serviceName);

}