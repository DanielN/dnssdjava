/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xbill.DNS.Address;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;


/**
 *
 * @author daniel.nilsson
 */
class UnicastDnsSDRegistrator implements DnsSDRegistrator {

	private static final Logger logger = Logger.getLogger(UnicastDnsSDBrowser.class.getName());

	private static final Name DNSUPDATE_UDP = Name.fromConstantString("_dns-update._udp");
	
	private final Name registrationDomain;
	private final Resolver resolver;

	UnicastDnsSDRegistrator(Name registrationDomain) throws UnknownHostException {
		this.registrationDomain = registrationDomain;
		this.resolver = findUpdateResolver(registrationDomain);
		logger.log(Level.INFO, "Created DNS-SD Registrator for domain {0}", registrationDomain);
	}

	private Resolver findUpdateResolver(Name domain) throws UnknownHostException {
		SimpleResolver simpleResolver = null;
		try {
			Lookup lookup = new Lookup(Name.concatenate(DNSUPDATE_UDP, domain), Type.SRV);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					if (record instanceof SRVRecord) {
						SRVRecord srv = (SRVRecord) record;
						simpleResolver = new SimpleResolver();
						InetAddress addr = Address.getByName(srv.getTarget().toString());
						InetSocketAddress socaddr = new InetSocketAddress(addr, srv.getPort());
						logger.log(Level.INFO, "Using DNS server {0} to perform updates.", socaddr);
						simpleResolver.setAddress(socaddr);
						break;
					}
				}
			}
		} catch (NameTooLongException e) {
			logger.log(Level.WARNING, "Failed to lookup update DNS server", e);
		}
		if (simpleResolver == null) {
			simpleResolver = new SimpleResolver();
		}
		return simpleResolver;
	}
	
	@Override
	public ServiceName makeServiceName(String name, ServiceType type) {
		return new ServiceName(name, type, registrationDomain.toString());
	}
	
	@Override
	public boolean registerService(ServiceData serviceData) throws DnsSDException {
		try {
			ServiceName serviceName = serviceData.getName();
			Name dnsName = serviceName.toDnsName();
			Name typeName = new Name(serviceName.getType().toString(), registrationDomain);
			Name target = new Name(serviceData.getHost());
			List<String> strings = new ArrayList<String>();
			for (Map.Entry<String, String> entry : serviceData.getProperties().entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getKey());
				if (entry.getValue() != null) {
					sb.append('=').append(entry.getValue());
				}
				strings.add(sb.toString());
			}
			if (strings.isEmpty()) {
				// Must not be empty
				strings.add("");
			}
			Update update = new Update(registrationDomain);		// XXX Should really be the zone (SOA) for the RRs we are about to add
			update.absent(dnsName);
			update.add(new PTRRecord(typeName, DClass.IN, 60, dnsName));
			update.add(new SRVRecord(dnsName, DClass.IN, 60, 0, 0, serviceData.getPort(), target));
			update.add(new TXTRecord(dnsName, DClass.IN, 60, strings));
			Message response = resolver.send(update);
			switch (response.getRcode()) {
				case Rcode.NOERROR:
					return true;
				case Rcode.YXDOMAIN:	// Prerequisite failed, the service already exists.
					return false;
				default:
					throw new DnsSDException("Server returned error code: " + Rcode.string(response.getRcode()));
			}
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid service data: " + serviceData, ex);
		} catch (IOException ex) {
			throw new DnsSDException("Failed to send DNS update to server", ex);
		}
	}
	
	@Override
	public boolean unregisterService(ServiceName serviceName) throws DnsSDException {
		try {
			Name dnsName = serviceName.toDnsName();
			Name typeName = new Name(serviceName.getType().toString(), registrationDomain);
			Update update = new Update(registrationDomain);		// XXX Should really be the zone (SOA) for the RRs we are about to remove
			update.present(dnsName);
			update.delete(new PTRRecord(typeName, DClass.IN, 60, dnsName));
			update.delete(dnsName);
			Message response = resolver.send(update);
			switch (response.getRcode()) {
				case Rcode.NOERROR:
					return true;
				case Rcode.NXDOMAIN:	// Prerequisite failed, the service doesn't exist.
					return false;
				default:
					throw new DnsSDException("Server returned error code: " + Rcode.string(response.getRcode()));
			}
		} catch (TextParseException ex) {
			throw new IllegalArgumentException("Invalid service name: " + serviceName, ex);
		} catch (IOException ex) {
			throw new DnsSDException("Failed to send DNS update to server", ex);
		}
	}
}
