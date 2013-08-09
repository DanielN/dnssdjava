/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Factory class for creating {@link DnsSDBrowser}, {@link DnsSDRegistrator} and
 * {@link DnsSDDomainEnumerator} objects.
 * @author Daniel Nilsson
 */
public abstract class DnsSDFactory {

	private static DnsSDFactory instance;

	/**
	 * Get the singleton factory object.
	 * @return the DnsSDFactory.
	 */
	public static synchronized DnsSDFactory getInstance() {
		if (instance == null) {
			String factoryClassName = System.getProperty("com.github.danieln.dnssdjava.factory");
			if (factoryClassName != null) {
				try {
					Class<?> factoryClass = Class.forName(factoryClassName);
					instance = (DnsSDFactory) factoryClass.newInstance();
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				instance = new UnicastDnsSDFactory();
			}
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
	public abstract DnsSDDomainEnumerator createDomainEnumerator(Collection<String> computerDomains);

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
		return createDomainEnumerator(getComputerDomains());
	}

	/**
	 * Try to figure out the domain name(s) for the computer.
	 * This includes reverse subnet addresses, as described in RFC 6763 chapter 11.
	 * @return a list of potential domain names.
	 */
	public List<String> getComputerDomains() {
		return DomainUtil.getComputerDomains();
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
	public abstract DnsSDBrowser createBrowser(Collection<String> browserDomains);

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
	public abstract DnsSDRegistrator createRegistrator(String registeringDomain) throws DnsSDException;

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
