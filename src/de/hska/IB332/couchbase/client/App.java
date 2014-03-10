package de.hska.IB332.couchbase.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App extends Application {
	
	private static TextArea textAreaReduce;
	private static TextArea textAreaMap;
	private static TextArea textAreaResults;
	private static CouchbaseService service;
	
	/**
	 * Starts the app. Read map/reduce functions from file, execute syntax checking, 
	 * creates view on Couchbase Server and get the Result of the view.
	 * @param args
	 */
	public static void main(String[] args) {
		initService();
		launch(args);
		
		System.exit(0);
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
	 * Writes file to disk.
	 * @param path
	 * @param content
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	static void writeFile(String path, String content) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(content);
		writer.close();
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

	
	/**
	 * Setup GUI.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setWidth(1024);
		primaryStage.setHeight(768);
		primaryStage.setTitle("Couchbase Labor Client");
		
		// ViewBox
		BorderPane pane = new BorderPane();
		
		// Setting up scene
		Scene scene = new Scene(pane);
		scene.getStylesheets().add("/de/hska/IB332/couchbase/client/layoutstyles.css");
		
    	// MenuBar
		MenuBar menuBar = new MenuBar();
		
		Menu fileMenu = new Menu("_Datei");
		fileMenu.setMnemonicParsing(true);
		
		// run code
		MenuItem menuItemRun = new MenuItem("Ausführen");
		menuItemRun.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				checkCode();
				
				service.createView("beginner_new", "new", textAreaMap.getText(), textAreaReduce.getText());
				
				for (ViewRow row : service.getView("beginner_new", "new", 1000)) {
					textAreaResults.setText(textAreaResults.getText() + "\n" + row.getKey() + " : " + row.getValue());
				}
			}
		});
		fileMenu.getItems().add(menuItemRun);
		
		// close
		MenuItem menuItemClose = new MenuItem("Schließen");
		menuItemClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.getItems().add(menuItemClose);
		
		menuBar.getMenus().add(fileMenu);
		pane.setTop(menuBar);
		
		// Textarea for map function
		VBox wrapperMapReduceFunctions = new VBox();
		TitledPane paneMapFunction = new TitledPane();
		paneMapFunction.setText("Map Funktion:");

		textAreaMap = new TextArea();
		textAreaMap.getStyleClass().add("map-reduce-area");
		paneMapFunction.setContent(textAreaMap);
		wrapperMapReduceFunctions.getChildren().add(paneMapFunction);

		// Textarea for reduce function
		TitledPane paneReduceFunction = new TitledPane();
		paneReduceFunction.setText("Reduce Funktion:");

		textAreaReduce = new TextArea();
		textAreaReduce.getStyleClass().add("map-reduce-area");
		paneReduceFunction.setContent(textAreaReduce);
		wrapperMapReduceFunctions.getChildren().add(paneReduceFunction);
		
		pane.setCenter(wrapperMapReduceFunctions);
		pane.getCenter().getStyleClass().add("center-pane");
		
		// Result
		TitledPane paneResults = new TitledPane();
		paneResults.setText("Ergebnis");
		
		textAreaResults = new TextArea();
		textAreaResults.setEditable(false);
		paneResults.setContent(textAreaResults);
		
		pane.setBottom(paneResults);
		
        primaryStage.setScene(scene);
		
		primaryStage.show();
	}
	
	private static void initService() {
		service = null;
		try {
			// get connection to database
			service = CouchbaseServiceFactory.getService(
					 "10.75.41.231", "Administrator", "adminadmin");
		} catch (Exception e) {
			e.printStackTrace();
			service.closeConnection();
			System.exit(1);
		}
	}
	
	private static void checkCode() {
		try {
			String mapFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\mapFunction.js";
			writeFile(mapFunctionPath, textAreaMap.getText());
			String checkResult = checkJavaScriptFile(mapFunctionPath);
			textAreaResults.setText(checkResult);
			
			String reduceFunctionPath = "C:\\Projects\\CouchbaseClient\\lib\\jsl\\reduceFunction.js";
			writeFile(reduceFunctionPath, textAreaReduce.getText());
			checkResult = checkJavaScriptFile(reduceFunctionPath);
			textAreaResults.setText(textAreaResults.getText() + "\n" + checkResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
