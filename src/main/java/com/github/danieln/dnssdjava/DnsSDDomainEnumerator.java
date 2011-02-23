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
public interface DnsSDDomainEnumerator {

	Collection<String> getBrowsingDomains();

	String getDefaultBrowsingDomain();

	String getDefaultRegisteringDomain();

	Collection<String> getLegacyBrowsingDomains();

	Collection<String> getRegisteringDomains();

}
