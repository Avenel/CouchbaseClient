package de.hska.IB332.couchbase.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App {

	/**
	 * Starts the app. Read map/reduce functions from file, execute syntax checking, 
	 * creates view on Couchbase Server and get the Result of the view.
	 * @param args
	 */
	public static void main(String[] args) {
		CouchbaseService service = null;
		try {
			// get connection to database
			service = CouchbaseServiceFactory.getService(
					 "10.75.41.231", "Administrator", "adminadmin");

			// Load and check mapFunction for errors
			// load mapFunction from file
			String mapFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\mapFunction.js";
			String mapFunction = readFile(mapFunctionPath, Charset.defaultCharset());
			System.out.println(mapFunction);
			System.out.println(checkJavaScriptFile(mapFunctionPath));

			// Load and check reduceFunction for errors
			// load reduceFunction from file
			String reduceFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\reduceFunction.js";
			String reduceFunction = readFile(reduceFunctionPath, Charset.defaultCharset());
			System.out.println(reduceFunction);
			System.out.println(checkJavaScriptFile(reduceFunctionPath));

			// create simple view
			String designDocumentName = "beginner";
			String viewName = "user_test";

			service.createView(designDocumentName, viewName, mapFunction, reduceFunction);
			
//			// get simple view
			ViewResponse response = service.getView(designDocumentName,
					viewName, 1000);
//
//			// Print value (5891 = beer count)
			for (ViewRow row : response) {
				System.out.println(row.getKey() + ": " + row.getValue());
			}
			
			service.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			service.closeConnection();
			System.exit(1);
		}
	}

	/**
	 * Read file into string.
	 * @param path
	 * @param encoding
	 * @return String
	 * @throws IOException
	 */
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	/**
	 * Check given JavaScript file for Syntax errors, by using JSL
	 * @param path
	 * @return String result
	 * @throws IOException
	 */
	static String checkJavaScriptFile(String path) throws IOException {
		Process process = new ProcessBuilder(
				"C:\\Projects\\CouchbaseClient\\lib\\jsl\\jsl.exe",
				"-process",
				path)
				.start();

		// get output from JSL
		String title = "Checking JavaScriptFile: " + path + "\n";
		BufferedReader input = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		
		String lines = "";
		String line;
		while ((line = input.readLine()) != null) {
			lines += line + "\n";
		}
		input.close();
		
		return title + lines;
	}

}
