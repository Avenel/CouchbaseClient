package de.hska.IB332.couchbase.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import de.hska.IB332.couchbase.client.CouchbaseResultRow;

public class AsyncGetViewCall implements Runnable {

	private ObservableList<CouchbaseResultRow> resultRows;
	HttpFuture<ViewResponse> result;
	ProgressIndicator progressIndicator;
	TextArea textAreaErrors;
	
	
	public AsyncGetViewCall(HttpFuture<ViewResponse> result, ObservableList<CouchbaseResultRow> resultRows, ProgressIndicator progressIndicator, TextArea textAreaErrors) {
		this.result = result;
		this.resultRows = resultRows;
		this.progressIndicator = progressIndicator;
		this.textAreaErrors = textAreaErrors;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Fetching view...");
			ViewResponse response = result.get(120, TimeUnit.SECONDS);
			
			if (response != null) {
				for (ViewRow row : response) {
					this.resultRows.add(new CouchbaseResultRow(row.getKey(), row.getValue()));
				}
			}
		} catch (InterruptedException e) {
			hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
		} catch (ExecutionException e) {
			hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
		} catch (TimeoutException e) {
			hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
		} 
		
		this.textAreaErrors.setText("Request ist erfolgreich gewesen. Rows: " + this.resultRows.size());
		hideProgressIndicator();
	}
	
	private void hideProgressIndicator() {
		// Hide ProgressIndicator
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	progressIndicator.visibleProperty().set(false);
	        }
	   });
	}

	
	
}
