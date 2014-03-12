package de.hska.IB332.couchbase.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;

public class CouchbaseService {
	
	private String url;
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
		this.url = "http://" + this.url;
	    nodes.add(URI.create(this.url + ":8091/pools"));
	    
	    // Try to connect to the client
	    CouchbaseConnectionFactoryBuilder couchbaseConnectionFactoryBuilder = new CouchbaseConnectionFactoryBuilder();
	    couchbaseConnectionFactoryBuilder.setOpTimeout(20000);
	    couchbaseConnectionFactoryBuilder.setViewTimeout(30000);
	    couchbaseConnectionFactoryBuilder.setTimeoutExceptionThreshold(10000);
	    
	    CouchbaseConnectionFactory connFactory = couchbaseConnectionFactoryBuilder.buildCouchbaseConnection(nodes, "world", "");

	    
	    this.client = null;
	    client = new CouchbaseClient(connFactory);//nodes, "beer-sample", this.user, this.password);
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

		ViewDesign view = new ViewDesign(viewName, mapFunction, reduceFunction);
		designDoc.setView(view);

		client.createDesignDoc(designDoc);
	}
	
	/**
	 * Returns the result of a MapReduce action of a given view.
	 * @param ddName
	 * @param viewName
	 * @param limit
	 * @return ViewResponse result
	 */
	public HttpFuture<ViewResponse> getView(String ddName, String viewName, int limit) {		
		// 1: Get the View definition from the cluster
		HttpFuture<View> view = client.asyncGetView(ddName, viewName);
		
		// 2: Create the query object
		Query query = new Query();
		query.setLimit(limit);
		query.setGroup(true);
		query.setStale(Stale.FALSE);

		// 3: Query the cluster with the view and query information
		HttpFuture<ViewResponse> result = null;
		try {
			// TODO Make it async
			result = client.asyncQuery(view.get(), query);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	
}
