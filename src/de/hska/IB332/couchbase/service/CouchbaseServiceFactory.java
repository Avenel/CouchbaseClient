package de.hska.IB332.couchbase.service;

import java.util.HashMap;

public class CouchbaseServiceFactory {

	private static HashMap<String, CouchbaseService> services;
	
	public static CouchbaseService getService(String url, String user, String password) throws Exception {
		if (services == null) {
			services = new HashMap<String, CouchbaseService>();
		}
		
		if (services.containsKey(url) == false) {
			CouchbaseService service = new CouchbaseService(url, user, password);
			services.put(url, service);
		}
		
		return services.get(url);
	}

}
