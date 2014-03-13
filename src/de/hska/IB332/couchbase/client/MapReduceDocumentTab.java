package de.hska.IB332.couchbase.client;

import java.io.Serializable;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
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
		
		Label docNameLabel = new Label();
		docNameLabel.setText(doc.getDesignDocName() + " : " + doc.getViewName());
		docNameLabel.getStyleClass().add("doc-name-label");
		wrapperMapReduceFunctions.getChildren().add(docNameLabel);
		
		
		TitledPane paneMapFunction = new TitledPane();
		paneMapFunction.setText("Map Funktion");
	
		final TextArea textAreaMap = new TextArea();
		textAreaMap.getStyleClass().add("map-reduce-area");

		paneMapFunction.setContent(textAreaMap);
		wrapperMapReduceFunctions.getChildren().add(paneMapFunction);
	
		// Textarea for reduce function
		TitledPane paneReduceFunction = new TitledPane();
		paneReduceFunction.setText("Reduce Funktion");
		
		final TextArea textAreaReduce = new TextArea();
		textAreaReduce.getStyleClass().add("map-reduce-area");
		
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
