/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author daniel.nilsson
 */
public class ServiceData {

	private ServiceName name;
	private String host;
	private int port;
	private Map<String, String> properties = new HashMap<String, String>();

	public ServiceData() {
	}

	public ServiceData(ServiceName name, String host, int port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public ServiceName getName() {
		return name;
	}

	public void setName(ServiceName name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return String.format("%s: %s:%d %s", name, host, port, properties);
	}

}
