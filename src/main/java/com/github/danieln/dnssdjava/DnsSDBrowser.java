/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.Collection;

/**
 * A DnsSDBrowser object provides methods for discovering services.
 * @author Daniel Nilsson
 */
public interface DnsSDBrowser {

	/**
	 * Get the service details for a service.
	 * @param service the name of the service.
	 * @return the service data.
	 */
	ServiceData getServiceData(ServiceName service);

	/**
	 * Get the names of all services of a certain type.
	 * If the type has one or more subtypes specified then the result
	 * is the union of services registered under those subtypes.
	 * @param type the service type to look up.
	 * @return a collection of service names.
	 */
	Collection<ServiceName> getServiceInstances(ServiceType type);

	/**
	 * Get the available service types.
	 * This only lists the base types without any subtypes.
	 * The DNS-SD RFC provides no way to enumerate subtypes.
	 * @return a collection of service types.
	 */
	Collection<ServiceType> getServiceTypes();

}
