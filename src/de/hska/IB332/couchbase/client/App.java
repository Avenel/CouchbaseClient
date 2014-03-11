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

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.ViewResponse;

import de.hska.IB332.couchbase.service.AsyncGetViewCall;
import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App extends Application {
	
	private static TextArea textAreaReduce;
	private static TextArea textAreaMap;
	private static TextArea textAreaResults;
	private static CouchbaseService service;
	private static ObservableList<CouchbaseResultRow> resultRows;
	private static TableView<CouchbaseResultRow> table;
	private static ProgressIndicator progressIndicator;
	private static TextArea textAreaErrors;
	
	/**
	 * Starts the app. Read map/reduce functions from file, execute syntax checking, 
	 * creates view on Couchbase Server and get the Result of the view.
	 * @param args
	 */
	public static void main(String[] args) {
		initService();
		launch(args);
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
				"lib/jsl/jsl.exe",
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
		// Load Font-Awesome icons
		Font.loadFont(App.class.getClassLoader().getResource("de/hska/IB332/couchbase/client/fonts/fontawesome-webfont.ttf").
	            toExternalForm(), 12);

		primaryStage.setWidth(1280);
		primaryStage.setHeight(1024);
		primaryStage.setTitle("Couchbase Labor Client");
		
		// ViewBox
		BorderPane pane = new BorderPane();
		
		// Setting up scene
		Scene scene = new Scene(pane);
		scene.getStylesheets().add("/de/hska/IB332/couchbase/client/layoutstyles.css");
		
    	// MenuBar
		VBox wrapperMenu = new VBox();
		MenuBar menuBar = new MenuBar();
		
		Menu fileMenu = new Menu("_Datei");
		fileMenu.setMnemonicParsing(true);
		
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
		
		// Toolbar
		ToolBar tools = new ToolBar();
		
		Button execute = AwesomeFactory.createIconButton(AwesomeIcons.ICON_PLAY_CIRCLE, "Ausführen", 30);
		execute.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				checkCode();
				
				service.createView("beginner_new", "new", textAreaMap.getText(), textAreaReduce.getText());
				
				resultRows.clear();
				// get view, async
				progressIndicator.visibleProperty().set(true);
				HttpFuture<ViewResponse> futureViewResponse = service.getView("beginner_new", "new", 1000);
				Thread fetchViewThread = new Thread(new AsyncGetViewCall(futureViewResponse, resultRows, progressIndicator, textAreaErrors));
				fetchViewThread.start();
			}
		});
		tools.getItems().add(execute);
		
		progressIndicator = new ProgressIndicator();
		progressIndicator.visibleProperty().set(false);
		tools.getItems().add(progressIndicator);
		
		wrapperMenu.getChildren().addAll(menuBar, tools);
		pane.setTop(wrapperMenu);
		
		// Textarea for map function
		VBox wrapperMapReduceFunctions = new VBox();
		TitledPane paneMapFunction = new TitledPane();
		paneMapFunction.setText("Map Funktion");

		textAreaMap = new TextArea();
		textAreaMap.getStyleClass().add("map-reduce-area");
		paneMapFunction.setContent(textAreaMap);
		wrapperMapReduceFunctions.getChildren().add(paneMapFunction);

		// Textarea for reduce function
		TitledPane paneReduceFunction = new TitledPane();
		paneReduceFunction.setText("Reduce Funktion");

		textAreaReduce = new TextArea();
		textAreaReduce.getStyleClass().add("map-reduce-area");
		paneReduceFunction.setContent(textAreaReduce);
		wrapperMapReduceFunctions.getChildren().add(paneReduceFunction);
		
		
		resetTextAreas();
		pane.setCenter(wrapperMapReduceFunctions);
		pane.getCenter().getStyleClass().add("center-pane");
		
		// Tabs for result and javascript code check
		TabPane tabPane = new TabPane();
		
		// Code Check Tab
		Tab tabCodeCheck = new Tab();
		tabCodeCheck.setText("JavaScript Check");
		tabCodeCheck.setClosable(false);
		
		textAreaResults = new TextArea();
		textAreaResults.setEditable(false);
		tabCodeCheck.setContent(textAreaResults);
		
		// Results Tab with a table view 
		Tab tabResults = new Tab();
		tabResults.setText("Ergebnis");
		tabResults.setClosable(false);
		
		table = new TableView<CouchbaseResultRow>();
	    resultRows = FXCollections.observableArrayList();
	    
	    table.setEditable(false);
	    table.setItems(resultRows);
	    
        TableColumn<CouchbaseResultRow, String> keyCol = new TableColumn<CouchbaseResultRow, String>("Key");
        keyCol.setMinWidth(100);
        keyCol.setCellValueFactory(
                new PropertyValueFactory<CouchbaseResultRow, String>("key"));
 
        TableColumn<CouchbaseResultRow, String> valueCol = new TableColumn<CouchbaseResultRow, String>("Value");
        valueCol.setMinWidth(100);
        valueCol.setCellValueFactory(
                new PropertyValueFactory<CouchbaseResultRow, String>("result"));
		
        table.getColumns().addAll(keyCol, valueCol);
        
        tabResults.setContent(table);
        
        // Error Console
        Tab tabConsole = new Tab();
        tabConsole.setText("Console");
        tabConsole.setClosable(false);
		
		textAreaErrors = new TextArea();
		textAreaErrors.setEditable(false);
		tabConsole.setContent(textAreaErrors);
        
        tabPane.getTabs().addAll(tabResults, tabCodeCheck, tabConsole);
        
		pane.setBottom(tabPane);
		
        primaryStage.setScene(scene);
		
		primaryStage.show();
	}
	
	/**
	 * Initialize Couchbase Service
	 */
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
	
	/**
	 * Checks the code.
	 */
	private static void checkCode() {
		try {
			String mapFunctionPath =  "user_functions/mapFunction.js";
			writeFile(mapFunctionPath, textAreaMap.getText());
			String checkResult = checkJavaScriptFile(mapFunctionPath);
			textAreaResults.setText(checkResult);
			
			String reduceFunctionPath = "user_functions/reduceFunction.js";
			writeFile(reduceFunctionPath, textAreaReduce.getText());
			checkResult = checkJavaScriptFile(reduceFunctionPath);
			textAreaResults.setText(textAreaResults.getText() + "\n" + checkResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void resetTextAreas() {
		textAreaMap.setText("function(doc, meta) {\n}");
		textAreaReduce.setText("function(key, values, rereduce) {\n}");
	}
	
}
