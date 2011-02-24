/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 *
 * @author daniel.nilsson
 */
public class DnsSDFactory {

	private static DnsSDFactory instance;

	public static synchronized DnsSDFactory getInstance() {
		if (instance == null) {
			instance = new DnsSDFactory();
		}
		return instance;
	}

	protected DnsSDFactory() {
	}

	public DnsSDDomainEnumerator createDomainEnumerator(String computerDomain) {
		try {
			return new UnicastDnsSDDomainEnumerator(Collections.singletonList(Name.fromString(computerDomain)));
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid domain name: " + computerDomain, ex);
		}
	}

	public DnsSDDomainEnumerator createDomainEnumerator() {
		return new UnicastDnsSDDomainEnumerator(DomainUtil.getComputerDomains());
	}

	public DnsSDBrowser createBrowser() {
		return createBrowser(createDomainEnumerator());
	}

	public DnsSDBrowser createBrowser(String browserDomain) {
		return createBrowser(Collections.singletonList(browserDomain));
	}

	public DnsSDBrowser createBrowser(Collection<String> browserDomains) {
		List<Name> domains = new ArrayList<Name>(browserDomains.size());
		for (String domain : browserDomains) {
			try {
				domains.add(Name.fromString(domain));
			} catch (TextParseException ex) {
				throw new IllegalArgumentException("Invalid domain name: " + domain, ex);
			}
		}
		return new UnicastDnsSDBrowser(domains);
	}

	public DnsSDBrowser createBrowser(DnsSDDomainEnumerator domainEnumerator) {
		Collection<String> list = domainEnumerator.getBrowsingDomains();
		if (list.isEmpty()) {
			String bd = domainEnumerator.getDefaultBrowsingDomain();
			if (bd != null) {
				list = Collections.singletonList(bd);
			} else {
				list = domainEnumerator.getLegacyBrowsingDomains();
			}
		}
		return createBrowser(list);
	}

	public DnsSDRegistrator createRegistrator() throws DnsSDException {
		return createRegistrator(createDomainEnumerator());
	}

	public DnsSDRegistrator createRegistrator(String registeringDomain) throws DnsSDException {
		try {
			return new UnicastDnsSDRegistrator(Name.fromString(registeringDomain));
		} catch (UnknownHostException ex) {
			throw new DnsSDException("Failed to find DNS update server for domain: " + registeringDomain, ex);
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid domain name: " + registeringDomain, ex);
		}
	}

	public DnsSDRegistrator createRegistrator(DnsSDDomainEnumerator domainEnumerator) throws DnsSDException {
		String registeringDomain = domainEnumerator.getDefaultRegisteringDomain();
		if (registeringDomain == null) {
			Collection<String> domains = domainEnumerator.getRegisteringDomains();
			if (!domains.isEmpty()) {
				registeringDomain = domains.iterator().next();
			} else {
				throw new DnsSDException("Failed to find any registering domain");
			}
		}
		return createRegistrator(registeringDomain);
	}
}
