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
 * Factory class for creating {@link DnsSDBrowser}, {@link DnsSDRegistrator} and
 * {@link DnsSDDomainEnumerator} objects.
 * @author Daniel Nilsson
 */
public class DnsSDFactory {

	private static DnsSDFactory instance;

	/**
	 * Get the singleton factory object.
	 * @return the DnsSDFactory.
	 */
	public static synchronized DnsSDFactory getInstance() {
		if (instance == null) {
			instance = new DnsSDFactory();
		}
		return instance;
	}

	protected DnsSDFactory() {
	}

	/**
	 * Create a {@link DnsSDDomainEnumerator} that finds the browsing
	 * and registration domains for the given computer domains.
	 * @param computerDomains the domain names to try.
	 * @return a new {@link DnsSDDomainEnumerator}.
	 */
	public DnsSDDomainEnumerator createDomainEnumerator(Collection<String> computerDomains) {
		List<Name> domains = new ArrayList<Name>(computerDomains.size());
		for (String domain : computerDomains) {
			try {
				domains.add(Name.fromString(domain));
			} catch (TextParseException ex) {
				throw new IllegalArgumentException("Invalid domain name: " + domain, ex);
			}
		}
		return new UnicastDnsSDDomainEnumerator(domains);
	}

	/**
	 * Create a {@link DnsSDDomainEnumerator} that finds the browsing
	 * and registration domains for the given computer domain.
	 * @param computerDomain the domain name.
	 * @return a new {@link DnsSDDomainEnumerator}.
	 */
	public DnsSDDomainEnumerator createDomainEnumerator(String computerDomain) {
		return createDomainEnumerator(Collections.singletonList(computerDomain));
	}

	/**
	 * Create a {@link DnsSDDomainEnumerator} that finds the browsing
	 * and registration domains for this computer.
	 * @return a new {@link DnsSDDomainEnumerator}.
	 */
	public DnsSDDomainEnumerator createDomainEnumerator() {
		return createDomainEnumerator(DomainUtil.getComputerDomains());
	}

	/**
	 * Create a {@link DnsSDBrowser} that finds services in the default
	 * browsing domains.
	 * @return a new {@link DnsSDBrowser}.
	 */
	public DnsSDBrowser createBrowser() {
		return createBrowser(createDomainEnumerator());
	}

	/**
	 * Create a {@link DnsSDBrowser} that finds services in the specified
	 * browsing domain.
	 * @param browserDomain the name of the domain to browse.
	 * @return a new {@link DnsSDBrowser}.
	 */
	public DnsSDBrowser createBrowser(String browserDomain) {
		return createBrowser(Collections.singletonList(browserDomain));
	}

	/**
	 * Create a {@link DnsSDBrowser} that finds services in the specified
	 * browsing domains.
	 * @param browserDomains collection of domain names to browse.
	 * @return a new {@link DnsSDBrowser}.
	 */
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

	/**
	 * Create a {@link DnsSDBrowser} that finds services in the
	 * browsing domains found by the specified {@link DnsSDDomainEnumerator}.
	 * @param domainEnumerator the domain enumerator to query for browser domains.
	 * @return a new {@link DnsSDBrowser}.
	 */
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

	/**
	 * Create a {@link DnsSDRegistrator} that registers services in the
	 * default registration domain.
	 * @return a new {@link DnsSDRegistrator}.
	 * @throws DnsSDException if the registrator can't be created.
	 */
	public DnsSDRegistrator createRegistrator() throws DnsSDException {
		return createRegistrator(createDomainEnumerator());
	}

	/**
	 * Create a {@link DnsSDRegistrator} that registers services in the
	 * specified registration domain.
	 * @param registeringDomain the domain name to register services.
	 * @return a new {@link DnsSDRegistrator}.
	 * @throws DnsSDException if the registrator can't be created.
	 */
	public DnsSDRegistrator createRegistrator(String registeringDomain) throws DnsSDException {
		try {
			return new UnicastDnsSDRegistrator(Name.fromString(registeringDomain));
		} catch (UnknownHostException ex) {
			throw new DnsSDException("Failed to find DNS update server for domain: " + registeringDomain, ex);
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid domain name: " + registeringDomain, ex);
		}
	}

	/**
	 * Create a {@link DnsSDRegistrator} that registers services in the
	 * registration domain found by the specified {@link DnsSDDomainEnumerator}.
	 * @param domainEnumerator the domain enumerator to query for registration domains.
	 * @return a new {@link DnsSDRegistrator}.
	 * @throws DnsSDException if the registrator can't be created.
	 */
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
