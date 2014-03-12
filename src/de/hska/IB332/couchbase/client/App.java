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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.ViewResponse;

import de.hska.IB332.couchbase.service.AsyncGetViewResponseCall;
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
	private static VBox paneProgressIndicator;
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

		// Toolbar
		ToolBar tools = new ToolBar();
		
		// New
		final Button newDocument = AwesomeFactory.createIconButton(
				AwesomeIcons.ICON_FILE, "Neu...", 30);
		Tooltip newDocumentToolTip = new Tooltip("Erstellt ein neues Dokument. (Ctrl+N)");
		newDocument.setTooltip(newDocumentToolTip);
		newDocument.setOnAction(new EventHandler<ActionEvent>() {
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

				DialogResponse resp = Dialogs.showCustomDialog(primaryStage, grid, "Bitte geben Sie den Namen des Design Dokuments und der View ein.", "Neues MapReduce Dokument", DialogOptions.OK_CANCEL, null);
				if (resp.compareTo(DialogResponse.OK) == 0) {
					if (designDocName.getText().compareTo("") != 0 && viewName.getText().compareTo("") != 0) {
					    tabPaneCenter.getTabs().add(
								new MapReduceDocumentTab(new MapReduceDocument(designDocName.getText(), viewName.getText())));
					    tabPaneCenter.getSelectionModel().selectLast();
					    addOnChangedHandlerTabCenter();
					} else {
						Dialogs.showErrorDialog(primaryStage, "Der Name des Design Dokuments und der View darf nicht leer sein.", "Das Dokument wurde nicht erstellt.", "Fehlermeldung");
					}
				}
			}
		});
		
		// Open
		final Button openDocument = AwesomeFactory.createIconButton(
				AwesomeIcons.ICON_FOLDER_OPEN, "Öffnen...", 30);
		Tooltip openDocumentToolTip = new Tooltip("Öffnet ein vorhandenes Dokument. (Ctrl+O)");
		openDocument.setTooltip(openDocumentToolTip);
		openDocument.setOnAction(new EventHandler<ActionEvent>() {
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
			         tabPaneCenter.getSelectionModel().selectLast();
			         addOnChangedHandlerTabCenter();
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
		
		// Save
		final Button saveDocument = AwesomeFactory.createIconButton(
				AwesomeIcons.ICON_SAVE, "Speichern...", 30);
		Tooltip saveDocumentToolTip = new Tooltip("Speichert das Dokument. (Ctrl+S)");
		saveDocument.setTooltip(saveDocumentToolTip);
		saveDocument.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				saveDocument(primaryStage);
			}
		});
		
		// Execute
		final Button execute = AwesomeFactory.createIconButton(
				AwesomeIcons.ICON_PLAY_CIRCLE, "Ausführen", 30);
		Tooltip executeToolTip = new Tooltip("Führt Query aus. (Ctrl+Enter)");
		execute.setTooltip(executeToolTip);
		execute.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				if (checkCode()) {
					service.createView("beginner_new", "new",
							getCurrentTextAreaMap().getText(),
							getCurrentTextAreaReduce().getText());

					resultRows.clear();
					// get view, async
					showProgressIndicator();
					HttpFuture<ViewResponse> futureViewResponse = service
							.getView("beginner_new", "new", 1000);
					Thread fetchViewThread = new Thread(new AsyncGetViewResponseCall(
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
		
		tools.getItems().addAll(newDocument, openDocument, saveDocument, execute);
		pane.setTop(tools);
		
		
		// ProgressIndicator for fetching view from server
		paneProgressIndicator = new VBox();
		progressIndicator = new ProgressIndicator();
		paneProgressIndicator.alignmentProperty().setValue(Pos.CENTER);
		paneProgressIndicator.getChildren().add(progressIndicator);
		

		// Textarea for map function
		tabPaneCenter = new TabPane();
		tabPaneCenter.getStyleClass().add("tab-pane-center");

		MapReduceDocumentTab untitledDocumentTab = new MapReduceDocumentTab(new MapReduceDocument("untitled", "untitled")); 
		tabPaneCenter.getTabs().add(untitledDocumentTab);
		addOnChangedHandlerTabCenter();
		
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
		
		// TODO "make it sexy"
//		scene.getRoot().getStyleClass().add("rootPane");
		primaryStage.setScene(scene);
//		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.show();
		
		// Accelerators
		// add accelerator for new document (Ctrl+N)
		newDocument.getScene().getAccelerators().put(
				  new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_ANY), 
				  new Runnable() {
				    @Override public void run() {
				      newDocument.fire();
				    }
				  }
				);
		
		// add accelerator for open document (Ctrl+O)
		openDocument.getScene().getAccelerators().put(
				  new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_ANY), 
				  new Runnable() {
				    @Override public void run() {
				    	openDocument.fire();
				    }
				  }
				);
		
		// add accelerator for save document (Ctrl+S)
		saveDocument.getScene().getAccelerators().put(
				  new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY), 
				  new Runnable() {
				    @Override public void run() {
				    	saveDocument.fire();
				    }
				  }
				);		
		
		// add accelerator for execute query (Ctrl+Enter)
		execute.getScene().getAccelerators().put(
				  new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_ANY), 
				  new Runnable() {
				    @Override public void run() {
				      execute.fire();
				    }
				  }
				);

		// add accelerator for closing tabs (Ctrl+W)
		scene.getAccelerators().put(
			      new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_ANY), 
			      new Runnable() {
			        @Override public void run() {
			        	// if document has changed or has not saved the document at all, ask if user want to save these changes
			        	if (tabPaneCenter.getSelectionModel().getSelectedItem().getText().contains("*") || 
			        			((MapReduceDocumentTab) tabPaneCenter.getSelectionModel().getSelectedItem()).getDocument().getTargetFile() == null) {
				        	DialogResponse response = Dialogs.showConfirmDialog(primaryStage,
				        		    "Sie haben das Dokument verändert, möchten Sie vor dem Schließen noch speichern?", "Dokument Speichern", "Dokument Speichern");
				        	
				        	if (response.compareTo(DialogResponse.YES) == 0) {
				        		saveDocument(primaryStage);
				        		tabPaneCenter.getTabs().remove(tabPaneCenter.getSelectionModel().getSelectedIndex());
				        	} 
				        	
				        	if (response.compareTo(DialogResponse.NO) == 0) {
				        		tabPaneCenter.getTabs().remove(tabPaneCenter.getSelectionModel().getSelectedIndex());
				        	}
			        	} else {
			        		tabPaneCenter.getTabs().remove(tabPaneCenter.getSelectionModel().getSelectedIndex());
			        	}
			        }
			      }
			    );
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
	
	private static void addOnChangedHandlerTabCenter() {
		final Tab currentTab = tabPaneCenter.getSelectionModel().getSelectedItem();
		getCurrentTextAreaMap().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		    	currentTab.setText(currentTab.getText().replace("*", "") + "*");
		    	((MapReduceDocumentTab) currentTab).getDocument().setMapFunction(getCurrentTextAreaMap().getText());
		    }
		});
		
		getCurrentTextAreaReduce().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		        currentTab.setText(currentTab.getText().replace("*", "") + "*");
		        ((MapReduceDocumentTab) currentTab).getDocument().setReduceFunction(getCurrentTextAreaReduce().getText());
		    }
		});
	}
	
	private static void saveDocument(final Stage primaryStage) {
		SingleSelectionModel<Tab> selectionModel = tabPaneCenter.getSelectionModel();
		MapReduceDocument doc = ((MapReduceDocumentTab)selectionModel.getSelectedItem()).getDocument();
		File file = doc.getTargetFile();
		
		// if document was not already saved, choose destination
		if (file == null) {
			FileChooser fileChooser = new FileChooser();

			// Set extension filter
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
					"MRDoc files (*.mrdoc)", "*.mrdoc");
			fileChooser.getExtensionFilters().add(extFilter);

			// Show save file dialog
			file = fileChooser.showSaveDialog(primaryStage);
			doc.setTargetFile(file);
		} 

		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);

			out.writeObject(doc);
			out.close();
			fileOut.close();
			
			// append .mrdoc if necessary
			if (!file.getPath().contains(".mrdoc")) {
				System.out.println("renamed");
				file.renameTo(new File(file.getAbsoluteFile()+".mrdoc"));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		// delete modifier '*'in tab name
		final Tab currentTab = tabPaneCenter.getSelectionModel().getSelectedItem();
		currentTab.setText(currentTab.getText().replace("*", ""));
	}
	
	public static void showProgressIndicator() {
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	tabPaneBottom.getTabs().get(0).setContent(paneProgressIndicator);
	        }
	   });
		
	}
	
	public static void hideProgressIndicator() {
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	tabPaneBottom.getTabs().get(0).setContent(table);
	        }
	   });
	}

}
