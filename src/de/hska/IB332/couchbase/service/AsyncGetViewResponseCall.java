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
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import de.hska.IB332.couchbase.client.App;
import de.hska.IB332.couchbase.client.CouchbaseResultRow;

public class AsyncGetViewResponseCall implements Runnable {

	private ObservableList<CouchbaseResultRow> resultRows;
	HttpFuture<ViewResponse> result;
	TextArea textAreaErrors;
	TabPane tabPane;
	
	
	public AsyncGetViewResponseCall(HttpFuture<ViewResponse> result, ObservableList<CouchbaseResultRow> resultRows, 
							ProgressIndicator progressIndicator, TextArea textAreaErrors, TabPane tabPane) {
		this.result = result;
		this.resultRows = resultRows;
		this.textAreaErrors = textAreaErrors;
		this.tabPane = tabPane;
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
			App.hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
			showConsole();
		} catch (ExecutionException e) {
			App.hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
			showConsole();
		} catch (TimeoutException e) {
			App.hideProgressIndicator();
			this.textAreaErrors.setText(e.getMessage());
			showConsole();
		} 
		
		App.hideProgressIndicator();
		this.textAreaErrors.setText("Request ist erfolgreich gewesen. Rows: " + this.resultRows.size());
	}

	private void showConsole() {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(2);
	}
	
}
