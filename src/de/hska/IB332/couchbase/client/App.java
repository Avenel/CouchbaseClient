package de.hska.IB332.couchbase.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
			// get connection to database
			CouchbaseService service = CouchbaseServiceFactory.getService(
					"localhost", "", "");

			// Load and check mapFunction for errors
			// load mapFunction from file
			String mapFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\mapFunction.js";
			String mapFunction = readFile(mapFunctionPath, Charset.defaultCharset());
			System.out.println(mapFunction);
			try {
				System.out.println(checkJavaScriptFile(mapFunctionPath));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Load and check reduceFunction for errors
			// load reduceFunction from file
			String reduceFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\reduceFunction.js";
			String reduceFunction = readFile(reduceFunctionPath, Charset.defaultCharset());
			System.out.println(reduceFunction);
			try {
				System.out.println(checkJavaScriptFile(reduceFunctionPath));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			// create simple view
			String designDocumentName = "beerTwo";
			String viewName = "by_name";
			service.createView(designDocumentName, viewName, mapFunction,
					reduceFunction);

			// get simple view
			ViewResponse response = service.getView(designDocumentName,
					viewName, 10);

			// Print value (5891 = beer count)
			for (ViewRow row : response) {
				System.out.println("Beer count: " + row.getValue());
			}

			service.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
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
