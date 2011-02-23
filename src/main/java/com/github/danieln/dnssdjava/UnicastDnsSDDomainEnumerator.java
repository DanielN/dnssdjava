/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

/**
 *
 * @author daniel.nilsson
 */
class UnicastDnsSDDomainEnumerator implements DnsSDDomainEnumerator {

	private static final Logger logger = Logger.getLogger(UnicastDnsSDDomainEnumerator.class.getName());

	private static final Name B_DNSSD_UDP = Name.fromConstantString("b._dns-sd._udp");
	private static final Name DB_DNSSD_UDP = Name.fromConstantString("db._dns-sd._udp");
	private static final Name R_DNSSD_UDP = Name.fromConstantString("r._dns-sd._udp");
	private static final Name DR_DNSSD_UDP = Name.fromConstantString("dr._dns-sd._udp");
	private static final Name LB_DNSSD_UDP = Name.fromConstantString("lb._dns-sd._udp");

	private final List<Name> computerDomains;

	UnicastDnsSDDomainEnumerator(List<Name> computerDomains) {
		this.computerDomains = computerDomains;
		logger.log(Level.INFO, "Created DNS-SD DomainEnumerator for computer domains: {0}", computerDomains);
	}

	@Override
	public Collection<String> getBrowsingDomains() {
		return getDomains(B_DNSSD_UDP);
	}

	@Override
	public String getDefaultBrowsingDomain() {
		return getDomain(DB_DNSSD_UDP);
	}

	@Override
	public Collection<String> getRegisteringDomains() {
		return getDomains(R_DNSSD_UDP);
	}

	@Override
	public String getDefaultRegisteringDomain() {
		return getDomain(DR_DNSSD_UDP);
	}

	@Override
	public Collection<String> getLegacyBrowsingDomains() {
		return getDomains(LB_DNSSD_UDP);
	}

	private Collection<String> getDomains(Name rrName) {
		List<String> results = new ArrayList<String>();
		for (Name domain : computerDomains) {
			results.addAll(getDomains(rrName, domain));
		}
		return results;
	}

	private String getDomain(Name rrName) {
		for (Name domain : computerDomains) {
			List<String> domains = getDomains(rrName, domain);
			if (!domains.isEmpty()) {
				return domains.get(0);
			}
		}
		return null;
	}

	private List<String> getDomains(Name rrName, Name domainName) {
		try {
			List<String> results = new ArrayList<String>();
			Lookup lookup = new Lookup(Name.concatenate(rrName, domainName), Type.PTR);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					if (record instanceof PTRRecord) {
						PTRRecord ptr = (PTRRecord) record;
						Name name = ptr.getTarget();
						results.add(name.toString());
					}
				}
			}
			return results;
		} catch (NameTooLongException ex) {
			throw new IllegalArgumentException("Domain name too long: " + domainName, ex);
		}
	}

}
