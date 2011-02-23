/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.io.IOException;

/**
 *
 * @author daniel.nilsson
 */
interface DnsSDRegistrator {

	ServiceName makeServiceName(String name, ServiceType type);

	void registerService(ServiceData serviceData) throws IOException;

	void unregisterService(ServiceName serviceName);

}