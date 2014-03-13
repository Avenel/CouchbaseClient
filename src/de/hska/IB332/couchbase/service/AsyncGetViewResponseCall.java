package de.hska.IB332.couchbase.service;

import java.util.concurrent.TimeUnit;

import javafx.collections.ObservableList;

import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.RowError;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import de.hska.IB332.couchbase.client.App;
import de.hska.IB332.couchbase.client.CouchbaseResultRow;

public class AsyncGetViewResponseCall implements Runnable {

	private ObservableList<CouchbaseResultRow> resultRows;
	HttpFuture<ViewResponse> result;
	
	
	public AsyncGetViewResponseCall(HttpFuture<ViewResponse> result, ObservableList<CouchbaseResultRow> resultRows) {
		this.result = result;
		this.resultRows = resultRows;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Fetching view...");
			ViewResponse response = result.get(120, TimeUnit.SECONDS);
			
			if (response != null) {
				for (RowError error : response.getErrors()) {
					App.appendLoggingMessage(error.getReason());
				}
				
				for (ViewRow row : response) {
					this.resultRows.add(new CouchbaseResultRow(row.getKey(), row.getValue()));
				}
			}			
		} catch(Exception e) {
			App.hideProgressIndicator();
			App.appendLoggingMessage(e.getMessage());
			App.showLogTab();
		}
		
		App.hideProgressIndicator();
		App.appendLoggingMessage("Request ist erfolgreich gewesen. Rows: " + this.resultRows.size());
	}

}
