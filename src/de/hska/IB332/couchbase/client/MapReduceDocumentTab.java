package de.hska.IB332.couchbase.client;

import java.io.Serializable;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MapReduceDocumentTab extends Tab implements Serializable {
	
	private static final long serialVersionUID = -4296966077177615438L;

	private MapReduceDocument document;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapReduceDocumentTab(MapReduceDocument doc) {
		super();
		this.document = doc;
		if (doc.getTargetFile() == null) {
			this.setText("untitled.mrdoc*");
		} else {
			this.setText(doc.getTargetFile().getName());
		}
		
		final VBox wrapperMapReduceFunctions = new VBox();
		wrapperMapReduceFunctions.getStyleClass().add("vbox-map-reduce");
		
		HBox wrapperTitle = new HBox();
		wrapperTitle.getStyleClass().add("hbox-title");
		
		Label iconDesignDocument = AwesomeFactory.createIconLabel(AwesomeIcons.ICON_FILE_TEXT, 30);
		iconDesignDocument.setTooltip(new Tooltip("Design Dokument Name"));
		wrapperTitle.getChildren().add(iconDesignDocument);

		Label designDocNameLabel = new Label();
		designDocNameLabel.setText(doc.getDesignDocName());
		designDocNameLabel.getStyleClass().add("doc-name-label");
		wrapperTitle.getChildren().add(designDocNameLabel);
		
		Label iconView = AwesomeFactory.createIconLabel(AwesomeIcons.ICON_EYE_OPEN, 30);
		iconView.setTooltip(new Tooltip("View Name"));
		wrapperTitle.getChildren().add(iconView);

		Label viewNameLabel = new Label();
		viewNameLabel.setText(doc.getViewName());
		viewNameLabel.getStyleClass().add("doc-name-label");
		wrapperTitle.getChildren().add(viewNameLabel);
		
		
		wrapperMapReduceFunctions.getChildren().add(wrapperTitle);
		
		
		TitledPane paneMapFunction = new TitledPane();
		paneMapFunction.setText("Map Funktion");
	
		final TextArea textAreaMap = new TextArea();
		textAreaMap.getStyleClass().add("map-reduce-area");
		textAreaMap.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent e) {
		        if (e.getCode() == KeyCode.TAB) {
		            textAreaMap.insertText(textAreaMap.getCaretPosition(), "   ");
		            e.consume();
		        }
		    }
		});
		
		paneMapFunction.setContent(textAreaMap);
		wrapperMapReduceFunctions.getChildren().add(paneMapFunction);
	
		// Textarea for reduce function
		TitledPane paneReduceFunction = new TitledPane();
		paneReduceFunction.setText("Reduce Funktion");
		
		final TextArea textAreaReduce = new TextArea();
		textAreaReduce.getStyleClass().add("map-reduce-area");
		textAreaReduce.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent e) {
		        if (e.getCode() == KeyCode.TAB) {
		            textAreaReduce.insertText(textAreaReduce.getCaretPosition(), "   ");
		            e.consume();
		        }
		    }
		});
		
		paneReduceFunction.setContent(textAreaReduce);
		wrapperMapReduceFunctions.getChildren().add(paneReduceFunction);
		
		loadTextAreas(textAreaMap, textAreaReduce, doc);
		
		// add height changed listener to vbox so textareas resize too
		wrapperMapReduceFunctions.heightProperty().addListener(new ChangeListener() {
	      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
	          textAreaMap.setPrefHeight(wrapperMapReduceFunctions.getHeight());
	          textAreaReduce.setPrefHeight(wrapperMapReduceFunctions.getHeight());
	        }
	      });
		
		this.setContent(wrapperMapReduceFunctions);
	}
	
	private void loadTextAreas(TextArea map, TextArea reduce, MapReduceDocument doc) {
		map.setText(doc.getMapFunction());
		reduce.setText(doc.getReduceFunction());
	}

	public MapReduceDocument getDocument() {
		return document;
	}

	public void setDocument(MapReduceDocument document) {
		this.document = document;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
