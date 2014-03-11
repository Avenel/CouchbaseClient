package de.hska.IB332.couchbase.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.ViewResponse;

import de.hska.IB332.couchbase.service.AsyncGetViewCall;
import de.hska.IB332.couchbase.service.CouchbaseService;
import de.hska.IB332.couchbase.service.CouchbaseServiceFactory;

public class App extends Application {

	private static TabPane tabPaneBottom;
	private static TabPane tabPaneCenter;
	private static TextArea currentTextAreaResults;
	private static TextArea currentTextAreaErrors;
	private static CouchbaseService service;
	private static ObservableList<CouchbaseResultRow> resultRows;
	private static TableView<CouchbaseResultRow> table;
	private static ProgressIndicator progressIndicator;

	/**
	 * Starts the app. Read map/reduce functions from file, execute syntax
	 * checking, creates view on Couchbase Server and get the Result of the
	 * view.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		initService();
		launch(args);
		
		System.exit(0);
	}

	/**
	 * Read file into string.
	 * 
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
	 * 
	 * @param path
	 * @param content
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	static void writeFile(String path, String content)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(content);
		writer.close();
	}

	/**
	 * Check given JavaScript file for Syntax errors, by using JSL
	 * 
	 * @param path
	 * @return String result
	 * @throws IOException
	 */
	static String checkJavaScriptFile(String path) throws IOException {
		Process process = new ProcessBuilder("lib/jsl/jsl.exe", "-process",
				path).start();

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
	public void start(final Stage primaryStage) throws Exception {
		// Load Font-Awesome icons
		Font.loadFont(
				App.class
						.getClassLoader()
						.getResource(
								"de/hska/IB332/couchbase/client/fonts/fontawesome-webfont.ttf")
						.toExternalForm(), 12);

		primaryStage.setWidth(1280);
		primaryStage.setHeight(1024);
		primaryStage.setTitle("Couchbase Labor Client");

		// ViewBox
		BorderPane pane = new BorderPane();

		// Setting up scene
		Scene scene = new Scene(pane);
		scene.getStylesheets().add(
				"/de/hska/IB332/couchbase/client/layoutstyles.css");

		// MenuBar
		VBox wrapperMenu = new VBox();
		MenuBar menuBar = new MenuBar();

		Menu fileMenu = new Menu("_Datei");
		fileMenu.setMnemonicParsing(true);

		// new
		MenuItem menuItemNew = new MenuItem("Neu...");
		menuItemNew.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				GridPane grid = new GridPane();
				grid.setHgap(10);
				grid.setVgap(10);
				grid.setPadding(new Insets(0, 10, 0, 10));
				final TextField designDocName = new TextField();
				designDocName.setPromptText("DesignDocument Name");
				final TextField viewName = new TextField();
				viewName.setPromptText("View Name");

				grid.add(new Label("Design Dokument:"), 0, 0);
				grid.add(designDocName, 1, 0);
				grid.add(new Label("View:"), 0, 1);
				grid.add(viewName, 1, 1);

				Callback<Void, Void> myCallback = new Callback<Void, Void>() {
					@Override
					  public Void call(Void param) {
						if (designDocName.getText().compareTo("") != 0 && viewName.getText().compareTo("") != 0) {
						    tabPaneCenter.getTabs().add(
									new MapReduceDocumentTab(new MapReduceDocument(designDocName.getText(), viewName.getText())));
						} else {
							Dialogs.showErrorDialog(primaryStage, "Der Name des Design Dokuments und der View darf nicht leer sein.", "Das Dokument wurde nicht erstellt.", "Fehlermeldung");
						}
					    return null;
					  }
					};
				
				DialogResponse resp = Dialogs.showCustomDialog(primaryStage, grid, "Bitte geben Sie den Namen des Design Dokuments und der View ein.", "Neues MapReduce Dokument", DialogOptions.OK_CANCEL, myCallback);

			}
		});

		// save
		MenuItem menuItemSave = new MenuItem("Speichern...");
		menuItemSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				FileChooser fileChooser = new FileChooser();

				// Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
						"MRDoc files (*.mrdoc)", "*.mrdoc");
				fileChooser.getExtensionFilters().add(extFilter);

				// Show save file dialog
				File file = fileChooser.showSaveDialog(primaryStage);

				try {
					FileOutputStream fileOut = new FileOutputStream(file);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);

					SingleSelectionModel<Tab> selectionModel = tabPaneCenter
							.getSelectionModel();
					out.writeObject(((MapReduceDocumentTab)selectionModel.getSelectedItem()).getDocument());
					out.close();
					fileOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		// load
		MenuItem menuItemLoad = new MenuItem("Laden...");
		menuItemLoad.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
			      try
			      {
			    	  FileChooser fileChooser = new FileChooser();

					// Set extension filter
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"MRDoc files (*.mrdoc)", "*.mrdoc");
					fileChooser.getExtensionFilters().add(extFilter);
					// Show save file dialog
					File file = fileChooser.showOpenDialog(primaryStage);
			    	  
			         FileInputStream fileIn = new FileInputStream(file);
			         ObjectInputStream in = new ObjectInputStream(fileIn);
			         MapReduceDocument mapReduceDocument = (MapReduceDocument) in.readObject();
			         in.close();
			         fileIn.close();
			         
			         tabPaneCenter.getTabs().add(new MapReduceDocumentTab(mapReduceDocument));
			      }catch(IOException i)
			      {
			         i.printStackTrace();
			         return;
			      }catch(ClassNotFoundException c)
			      {
			         System.out.println("Employee class not found");
			         c.printStackTrace();
			         return;
			      }
			}
		});

		// close
		MenuItem menuItemClose = new MenuItem("Schließen");
		menuItemClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				System.exit(0);
			}
		});

		fileMenu.getItems().addAll(menuItemNew, menuItemSave, menuItemLoad, menuItemClose);

		menuBar.getMenus().add(fileMenu);

		// Toolbar
		ToolBar tools = new ToolBar();

		Button execute = AwesomeFactory.createIconButton(
				AwesomeIcons.ICON_PLAY_CIRCLE, "Ausführen", 30);
		execute.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				if (checkCode()) {
					service.createView("beginner_new", "new",
							getCurrentTextAreaMap().getText(),
							getCurrentTextAreaReduce().getText());

					resultRows.clear();
					// get view, async
					progressIndicator.visibleProperty().set(true);
					HttpFuture<ViewResponse> futureViewResponse = service
							.getView("beginner_new", "new", 1000);
					Thread fetchViewThread = new Thread(new AsyncGetViewCall(
							futureViewResponse, resultRows, progressIndicator,
							currentTextAreaErrors, tabPaneBottom));
					fetchViewThread.start();

					SingleSelectionModel<Tab> selectionModel = tabPaneBottom
							.getSelectionModel();
					selectionModel.select(0);
				} else {
					SingleSelectionModel<Tab> selectionModel = tabPaneBottom
							.getSelectionModel();
					selectionModel.select(1);
				}
			}
		});
		tools.getItems().add(execute);

		progressIndicator = new ProgressIndicator();
		progressIndicator.visibleProperty().set(false);
		tools.getItems().add(progressIndicator);

		wrapperMenu.getChildren().addAll(menuBar, tools);
		pane.setTop(wrapperMenu);

		// Textarea for map function
		tabPaneCenter = new TabPane();
		tabPaneCenter.getStyleClass().add("tab-pane-center");

		tabPaneCenter.getTabs().add(new MapReduceDocumentTab(new MapReduceDocument("untitled", "untitled")));

		pane.setCenter(tabPaneCenter);
		pane.getCenter().getStyleClass().add("pane-center");

		// Tabs for result and javascript code check
		tabPaneBottom = new TabPane();

		// Code Check Tab
		Tab tabCodeCheck = new Tab();
		tabCodeCheck.setText("JavaScript Check");
		tabCodeCheck.setClosable(false);

		currentTextAreaResults = new TextArea();
		currentTextAreaResults.setEditable(false);
		tabCodeCheck.setContent(currentTextAreaResults);

		// Results Tab with a table view
		Tab tabResults = new Tab();
		tabResults.setText("Ergebnis");
		tabResults.setClosable(false);

		table = new TableView<CouchbaseResultRow>();
		resultRows = FXCollections.observableArrayList();

		table.setEditable(false);
		table.setItems(resultRows);

		TableColumn<CouchbaseResultRow, String> keyCol = new TableColumn<CouchbaseResultRow, String>(
				"Key");
		keyCol.setMinWidth(100);
		keyCol.setCellValueFactory(new PropertyValueFactory<CouchbaseResultRow, String>(
				"key"));

		TableColumn<CouchbaseResultRow, String> valueCol = new TableColumn<CouchbaseResultRow, String>(
				"Value");
		valueCol.setMinWidth(100);
		valueCol.setCellValueFactory(new PropertyValueFactory<CouchbaseResultRow, String>(
				"result"));

		table.getColumns().addAll(keyCol, valueCol);

		tabResults.setContent(table);

		// Error Console
		Tab tabConsole = new Tab();
		tabConsole.setText("Console");
		tabConsole.setClosable(false);

		currentTextAreaErrors = new TextArea();
		currentTextAreaErrors.setEditable(false);
		tabConsole.setContent(currentTextAreaErrors);

		tabPaneBottom.getTabs().addAll(tabResults, tabCodeCheck, tabConsole);

		pane.setBottom(tabPaneBottom);

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
			service = CouchbaseServiceFactory.getService("10.75.41.231",
					"Administrator", "adminadmin");
		} catch (Exception e) {
			e.printStackTrace();
			service.closeConnection();
			System.exit(1);
		}
	}

	/**
	 * Checks the code.
	 */
	private static boolean checkCode() {
		try {
			String mapFunctionPath = "user_functions/mapFunction.js";
			writeFile(mapFunctionPath, getCurrentTextAreaMap().getText());
			String checkResult = checkJavaScriptFile(mapFunctionPath);
			currentTextAreaResults.setText(checkResult);

			if (!checkResult.contains("0 error(s), 0 warning(s)")) {
				return false;
			}

			String reduceFunctionPath = "user_functions/reduceFunction.js";
			writeFile(reduceFunctionPath, getCurrentTextAreaReduce().getText());
			checkResult = checkJavaScriptFile(reduceFunctionPath);
			currentTextAreaResults.setText(currentTextAreaResults.getText()
					+ "\n" + checkResult);

			if (!checkResult.contains("0 error(s), 0 warning(s)")) {
				return false;
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	private static TextArea getCurrentTextAreaMap() {
		Tab currentTab = tabPaneCenter.getSelectionModel().getSelectedItem();
		return (TextArea) ((TitledPane) ((VBox) currentTab.getContent())
				.getChildren().get(0)).getContent();
	}

	private static TextArea getCurrentTextAreaReduce() {
		Tab currentTab = tabPaneCenter.getSelectionModel().getSelectedItem();
		return (TextArea) ((TitledPane) ((VBox) currentTab.getContent())
				.getChildren().get(1)).getContent();
	}

}
