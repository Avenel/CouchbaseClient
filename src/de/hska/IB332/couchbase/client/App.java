package de.hska.IB332.couchbase.client;

import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CouchbaseService service = CouchbaseServiceFactory.getService("localhost", "Administrator", "");
			
			service.createView("dev_beer", "by_name1");
			
			service.closeConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
