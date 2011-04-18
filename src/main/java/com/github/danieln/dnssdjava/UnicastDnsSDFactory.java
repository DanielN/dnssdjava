/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 * Unicast {@link DnsSDFactory} implementation backed by dnsjava.
 * @author Daniel Nilsson
 */
public class UnicastDnsSDFactory extends DnsSDFactory {

	UnicastDnsSDFactory() {
	}

	@Override
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

	@Override
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

	@Override
	public DnsSDRegistrator createRegistrator(String registeringDomain) throws DnsSDException {
		try {
			return new UnicastDnsSDRegistrator(Name.fromString(registeringDomain));
		} catch (UnknownHostException ex) {
			throw new DnsSDException("Failed to find DNS update server for domain: " + registeringDomain, ex);
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid domain name: " + registeringDomain, ex);
		}
	}

}
