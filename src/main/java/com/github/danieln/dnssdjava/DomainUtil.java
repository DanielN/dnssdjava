/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbill.DNS.Address;
import org.xbill.DNS.Name;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;

/**
 * Internal helper class for figuring out domain names.
 * @author Daniel Nilsson
 */
class DomainUtil {

	private static final Logger logger = Logger.getLogger(DomainUtil.class.getName());

	/**
	 * Try to figure out the domain name for the computer.
	 * @return a list of potential domain names.
	 */
	static List<String> getComputerDomains() {
		String domain = System.getProperty("dnssd.domain");
		if (domain != null) {
			return Collections.singletonList(domain);
		}
		List<String> results = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();
				if (i.isUp() && !i.isLoopback()) {
					for (InterfaceAddress ifaddr : i.getInterfaceAddresses()) {
						InetAddress inetAddr = ifaddr.getAddress();
						try {
							// Try to figure out the domain by taking the host name...
							String hostname = Address.getHostName(inetAddr);
							// ...and remove the leftmost part.
							results.add(new Name(new Name(hostname), 1).toString());
						} catch (TextParseException ex) {
							logger.log(Level.WARNING, "Bad hostname", ex);
						} catch (UnknownHostException ex) {
							logger.log(Level.FINE, "No hostname for address: {0}", inetAddr);
						}
						try {
							// Use the reverse lookup name for the network
							InetAddress network = calculateNetworkAddress(ifaddr);
							Name revName = ReverseMap.fromAddress(network);
							results.add(revName.toString());
						} catch (UnknownHostException ex) {
							logger.log(Level.WARNING, "Failed to calculate network address", ex);
						}
					}
				}
			}
		} catch (SocketException ex) {
			logger.log(Level.WARNING, "Failed to enumerate network interfaces", ex);
		}
		return results;
	}

	/**
	 * Try to figure out the host name for the computer.
	 * @return a list of potential host names.
	 */
	static List<String> getComputerHostNames() {
		String hostname = System.getProperty("dnssd.hostname");
		if (hostname != null) {
			return Collections.singletonList(hostname);
		}
		List<String> results = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();
				if (i.isUp() && !i.isLoopback()) {
					for (InterfaceAddress ifaddr : i.getInterfaceAddresses()) {
						InetAddress inetAddr = ifaddr.getAddress();
						try {
							hostname = Address.getHostName(inetAddr);
							results.add(hostname);
						} catch (UnknownHostException ex) {
							logger.log(Level.FINE, "No hostname for address: {0}", inetAddr);
						}
					}
				}
			}
		} catch (SocketException ex) {
			logger.log(Level.WARNING, "Failed to enumerate network interfaces", ex);
		}
		return results;
	}

	/**
	 * Calculate the network address by taking the bitwise AND
	 * between the IP-address and the netmask.
	 * @param ifaddr the interface address to calculate the network address of. 
	 * @return the network address (host part is all zero).
	 * @throws UnknownHostException if something went terribly wrong.
	 */
	static InetAddress calculateNetworkAddress(InterfaceAddress ifaddr) throws UnknownHostException {
		byte[] addr = ifaddr.getAddress().getAddress();
		int n = ifaddr.getNetworkPrefixLength();
		int i = n / 8;
		int j = n % 8;
		if (i < addr.length) {
			byte mask = (byte) (0xFF00 >> j);
			addr[i] &= mask;
			Arrays.fill(addr, i+1, addr.length, (byte) 0);
		}
		return InetAddress.getByAddress(addr);
	}

}
