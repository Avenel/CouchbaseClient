package de.hska.IB332.couchbase.service;

import java.net.URI;
import java.util.ArrayList;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.ViewDesign;

public class CouchbaseService {
	
	private String url, user, password;
	private CouchbaseClient client;
	
	public CouchbaseService(String url, String user, String password) throws Exception {
		this.url = url;
		this.user = user;
		this.password = password;
		connect();
	}
	
	public void connect() throws Exception {
		System.setProperty("viewmode", "development");
		ArrayList<URI> nodes = new ArrayList<URI>();

	    // Add one or more nodes of your cluster (exchange the IP with yours)
	    nodes.add(URI.create("http://"+this.url+":8091/pools"));

	    CouchbaseConnectionFactoryBuilder m_couchbaseConnectionFactoryBuilder = new CouchbaseConnectionFactoryBuilder();
	    m_couchbaseConnectionFactoryBuilder.setMaxReconnectDelay(10000);
	    m_couchbaseConnectionFactoryBuilder.setOpQueueMaxBlockTime(100);
	    m_couchbaseConnectionFactoryBuilder.setOpTimeout(20000);
	    m_couchbaseConnectionFactoryBuilder.setShouldOptimize(true);
	    m_couchbaseConnectionFactoryBuilder.setViewTimeout(300000);

	    CouchbaseConnectionFactory connFactory = m_couchbaseConnectionFactoryBuilder.buildCouchbaseConnection(nodes, "beer-sample", "");
	    
	    // Try to connect to the client
	    this.client = null;
	    client = new CouchbaseClient(connFactory);//this.user, this.password);
	}
	
	public void closeConnection() {
		// Shutdown the client
	    this.client.shutdown();
	}
	
	
	/**
	 * This function builds a map/reduce view
	 * @param bucket
	 * @param viewName
	 */
	public void createView(String bucket, String viewName) {
		DesignDocument designDoc = new DesignDocument(bucket);
		
		String mapFunction = 
				"function (doc, meta) {\n" +
				"  if (doc.type && doc.type == 'beer') {\n" +
				"    emit(doc.name);\n" +
				"  }\n" +
				"}";
		
		String reduceFunction = "";
		
		ViewDesign viewDesign = new ViewDesign(viewName, mapFunction, reduceFunction);
		designDoc.getViews().add(viewDesign);
		this.client.createDesignDoc(designDoc);
	}
	
}
