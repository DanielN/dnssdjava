/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.Collection;

/**
 * A DnsSDDomainEnumerator object provides methods for finding out
 * which domains to use for registering and browsing for services.
 * @author Daniel Nilsson
 */
public interface DnsSDDomainEnumerator {

	/**
	 * Get the list of domains recommended for browsing.
	 * @return a collection of domain names.
	 */
	Collection<String> getBrowsingDomains();

	/**
	 * Get the recommended default domain for browsing.
	 * @return a domain name.
	 */
	String getDefaultBrowsingDomain();

	/**
	 * Get the recommended default domain for registering services.
	 * @return a domain name.
	 */
	String getDefaultRegisteringDomain();

	/**
	 * Get the "legacy browsing" or "automatic browsing" domains.
	 * @return a collection of domain names.
	 */
	Collection<String> getLegacyBrowsingDomains();

	/**
	 * Get the list of domains recommended for registering services.
	 * @return a collection of domain names.
	 */
	Collection<String> getRegisteringDomains();

}
