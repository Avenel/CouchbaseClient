package de.hska.IB332.couchbase.client;

import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CouchbaseService service = CouchbaseServiceFactory.getService("localhost", "", "");
			
			// create simple view
			String mapFunction =
		            "function (doc, meta) {\n" +
		            "  if(doc.type && doc.type == \"beer\") {\n" +
		            "    emit(doc.name);\n" +
		            "  }\n" +
		            "}";
			
			String reduceFunction = "_count";
			
			service.createView("beerTwo", "by_name", mapFunction, reduceFunction);
			
			// get simple view
			ViewResponse response = service.getView("beerTwo", "by_name", 10);
			
			// Print value (5891 = beer count)
			for(ViewRow row : response) {
				System.out.println("Beer count: " + row.getValue());
			}
			
			service.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

}
