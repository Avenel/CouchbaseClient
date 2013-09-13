package de.hska.IB332.couchbase.service;

import java.net.URI;
import java.util.ArrayList;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;

public class CouchbaseService {
	
	private String url, user, password;
	private CouchbaseClient client;
	
	/**
	 * public constructor. Connects to CouchBase Server.
	 * @param url
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	public CouchbaseService(String url, String user, String password) throws Exception {
		this.url = url;
		this.user = user;
		this.password = password;
		connect();
	}
	
	/**
	 * Connects to CouchBase Server
	 * @throws Exception
	 */
	public void connect() throws Exception {
		System.setProperty("viewmode", "development");
		ArrayList<URI> nodes = new ArrayList<URI>();

	    // Add one or more nodes of your cluster (exchange the IP with yours)
	    nodes.add(URI.create("http://"+this.url+":8091/pools"));
	    
	    // Try to connect to the client
	    this.client = null;
	    client = new CouchbaseClient(nodes, "beer-sample", this.user, this.password);
	}
	
	/**
	 * Close connection.
	 */
	public void closeConnection() {
		// Shutdown the client
	    this.client.shutdown();
	}
	
	
	/**
	 * Builds a map/reduce view.
	 * @param bucket
	 * @param viewName
	 */
	public void createView(String ddName, String viewName, String mapFunction, String reduceFunction) {
		DesignDocument designDoc = new DesignDocument(ddName);		
		
		ViewDesign viewDesign = new ViewDesign(viewName, mapFunction, reduceFunction);
		designDoc.getViews().add(viewDesign);
		
		this.client.createDesignDoc(designDoc);
	}
	
	/**
	 * Returns the result of a MapReduce action of a given view.
	 * @param ddName
	 * @param viewName
	 * @param limit
	 * @return ViewResponse result
	 */
	public ViewResponse getView(String ddName, String viewName, int limit) {
		View view = client.getView(ddName, viewName);
		Query query = new Query();
		query.setIncludeDocs(true).setLimit(limit);
		query.setStale( Stale.FALSE );
		
		ViewResponse result = client.query(view, query);
		return result;
	}
	
	
}
