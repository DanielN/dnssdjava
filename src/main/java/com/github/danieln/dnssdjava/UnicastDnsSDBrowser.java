/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 *
 * @author daniel.nilsson
 */
class UnicastDnsSDBrowser implements DnsSDBrowser {

	private static final Logger logger = Logger.getLogger(UnicastDnsSDBrowser.class.getName());

	private static final Name SERVICES_DNSSD_UDP = Name.fromConstantString("_services._dns-sd._udp");

	private final List<Name> browserDomains;

	UnicastDnsSDBrowser(List<Name> browserDomains) {
		this.browserDomains = browserDomains;
		logger.log(Level.INFO, "Created DNS-SD Browser for domains: {0}", browserDomains);
	}

	@Override
	public Collection<ServiceType> getServiceTypes() {
		Set<ServiceType> results = new HashSet<ServiceType>();
		for (Name domain : browserDomains) {
			results.addAll(getServiceTypes(domain));
		}
		return results;
	}

	@Override
	public Collection<ServiceName> getServiceInstances(ServiceType type) {
		List<ServiceName> results = new ArrayList<ServiceName>();
		for (Name domain : browserDomains) {
			results.addAll(getServiceInstances(type, domain));
		}
		return results;
	}

	@Override
	public ServiceData getServiceData(ServiceName service) {
		Name serviceName = service.toDnsName();
		Lookup lookup = new Lookup(serviceName, Type.ANY);
		Record[] records = lookup.run();
		if (records == null || records.length == 0) {
			return null;
		}
		ServiceData data = new ServiceData();
		data.setName(service);
		for (Record record : records) {
			if (record instanceof SRVRecord) {
				// TODO Handle priority and weight correctly in case of multiple SRV record.
				SRVRecord srv = (SRVRecord) record;
				data.setHost(srv.getTarget().toString());
				data.setPort(srv.getPort());
			} else if (record instanceof TXTRecord) {
				// TODO Handle multiple TXT records as different variants of same service
				TXTRecord txt = (TXTRecord) record;
				for (Object o : txt.getStrings()) {
					String string = (String) o;     // Safe cast
					int i = string.indexOf('=');
					String key;
					String value;
					if (i == 0 || string.isEmpty()) {
						continue;   // Invalid empty key, should be ignored
					} else if (i > 0) {
						key = string.substring(0, i).toLowerCase();
						value = string.substring(i + 1);
					} else {
						key = string;
						value = null;
					}
					if (!data.getProperties().containsKey(key)) {   // Ignore all but the first
						data.getProperties().put(key, value);
					}
				}
			}
		}
		return data;
	}

	private List<ServiceType> getServiceTypes(Name domainName) {
		try {
			List<ServiceType> results = new ArrayList<ServiceType>();
			Lookup lookup = new Lookup(Name.concatenate(SERVICES_DNSSD_UDP, domainName), Type.PTR);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					if (record instanceof PTRRecord) {
						PTRRecord ptr = (PTRRecord) record;
						Name name = ptr.getTarget();
						String type = name.getLabelString(0);
						String transport = name.getLabelString(1);
						results.add(new ServiceType(type, transport));
					}
				}
			}
			return results;
		} catch (NameTooLongException ex) {
			throw new IllegalArgumentException("Too long name: " + domainName, ex);
		}
	}

	private List<ServiceName> getServiceInstances(ServiceType type, Name domainName) {
		try {
			Name typeDomainName = Name.fromString(type.toString(), domainName);
			List<ServiceName> results = new ArrayList<ServiceName>();
			Lookup lookup = new Lookup(typeDomainName, Type.PTR);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					if (record instanceof PTRRecord) {
						PTRRecord ptr = (PTRRecord) record;
						Name name = ptr.getTarget();
						results.add(ServiceName.fromDnsName(name));
					}
				}
			}
			return results;
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid type: " + type, ex);
		}
	}

}
