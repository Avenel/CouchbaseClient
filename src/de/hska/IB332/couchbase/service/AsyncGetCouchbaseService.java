package de.hska.IB332.couchbase.service;

import de.hska.IB332.couchbase.client.App;

public class AsyncGetCouchbaseService implements Runnable {
	
	public AsyncGetCouchbaseService() {
		super();
	}

	@Override
	public void run() {
		CouchbaseService service = null;
		try {
			// get connection to database
			service = CouchbaseServiceFactory.getService("10.75.41.231",
					"Administrator", "adminadmin");
			
			App.appendLoggingMessage("Verbindung hergestellt.");
			App.showLogTab();
			App.setCouchbaseService(service);
		} catch (Exception e) {
			if (service != null) {
				service.closeConnection();
			}
			App.appendLoggingMessage(e.getMessage());
			App.showLogTab();
		}
	}

}
