/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.Collection;

/**
 *
 * @author daniel.nilsson
 */
public interface DnsSDBrowser {

	ServiceData getServiceData(ServiceName service);

	Collection<ServiceName> getServiceInstances(ServiceType type);

	Collection<ServiceType> getServiceTypes();

}
