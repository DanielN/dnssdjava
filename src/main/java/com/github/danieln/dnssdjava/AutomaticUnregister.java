/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.HashSet;
import java.util.Set;


/**
 * Automatically unregister services on JVM shutdown.
 * This class uses a shutdown hook to unregister services when
 * the application exits. Services may not be unregistered on a
 * JMV crash or other abnormal termination.
 * @author Daniel Nilsson
 */
public class AutomaticUnregister {

	private final DnsSDRegistrator registrator;
	private final Set<ServiceName> serviceNames = new HashSet<ServiceName>();
	private final Thread shutdownHook;

	/**
	 * Create a new AutomaticUnregister object.
	 * @param registrator the DnsSDRegistrator to use.
	 */
	public AutomaticUnregister(DnsSDRegistrator registrator) {
		this.registrator = registrator;
		this.shutdownHook = new Thread(getClass().getSimpleName() + ".shutdownHook") {
			@Override
			public void run() {
				unregisterAll();
			}
		};
	}

	/**
	 * Add a service for automatic unregistration.
	 * @param serviceName the service name.
	 */
	public synchronized void addService(ServiceName serviceName) {
		if (serviceNames.isEmpty()) {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		serviceNames.add(serviceName);
	}
	
	/**
	 * Remove a service from automatic unregistration.
	 * @param serviceName the service name.
	 */
	public synchronized void removeService(ServiceName serviceName) {
		serviceNames.remove(serviceName);
		if (serviceNames.isEmpty()) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}
	
	/**
	 * Called from the shutdown hook to unregister the services.
	 */
	private synchronized void unregisterAll() {
		// because this code runs in a shutdown hook it doesn't use the logger
		for (ServiceName serviceName : serviceNames) {
			try {
				registrator.unregisterService(serviceName);
			} catch (DnsSDException e) {
				System.err.printf("WARNING: Failed to unregister service %s: %s\n", serviceName, e.getMessage());
			}
		}
	}
	
}
