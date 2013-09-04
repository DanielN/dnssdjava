package com.github.danieln.dnssdjava;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test application for doing testing during development.
 * The code is not meant as an example of how to use the library.
 * @author Daniel Nilsson
 */
public class TestApp {

	public static void main(String args[]) {

		Logger.getLogger("com.github.danieln.dnssdjava").setLevel(Level.ALL);
		DnsSDDomainEnumerator dom = DnsSDFactory.getInstance().createDomainEnumerator();
		
		String nameString = null;
		try {
			DnsSDRegistrator reg = DnsSDFactory.getInstance().createRegistrator(dom);
			ServiceName name = reg.makeServiceName("My\\Test.Service", ServiceType.valueOf("_http._tcp,_printer"));
			nameString = name.toString();
			ServiceData data = new ServiceData(name, reg.getLocalHostName(), 8080);
			if (reg.registerService(data)) {
				System.out.println("Service registered: " + name);
			} else {
				System.out.println("Service already exists: " + name);
			}
		} catch (DnsSDException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		DnsSDBrowser dnssd = DnsSDFactory.getInstance().createBrowser(dom);
		Collection<ServiceType> types = dnssd.getServiceTypes();
		System.out.println(types);
		for (ServiceType type : types) {
			Collection<ServiceName> instances = dnssd.getServiceInstances(type);
			System.out.println(instances);
			for (ServiceName instance : instances) {
				ServiceData service = dnssd.getServiceData(instance);
				System.out.println(service);
			}
		}

		try {
			DnsSDRegistrator reg = DnsSDFactory.getInstance().createRegistrator(dom);
			ServiceName name = ServiceName.valueOf(nameString);
			if (reg.unregisterService(name)) {
				System.out.println("Service unregistered: " + name);
			} else {
				System.out.println("No service to remove: " + name);
			}
		} catch (DnsSDException e) {
			e.printStackTrace();
		}

	}

}
