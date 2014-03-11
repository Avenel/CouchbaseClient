package de.hska.IB332.couchbase.client;

import java.io.Serializable;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class MapReduceDocumentTab extends Tab implements Serializable {
	
	private static final long serialVersionUID = -4296966077177615438L;

	private MapReduceDocument document;
	
	public MapReduceDocumentTab(MapReduceDocument doc) {
		super();
		this.document = doc;
		this.setText(doc.getDesignDocName() + "_" + doc.getViewName());
		
		VBox wrapperMapReduceFunctions = new VBox();
		wrapperMapReduceFunctions.getStyleClass().add("vbox-map-reduce");
		TitledPane paneMapFunction = new TitledPane();
		paneMapFunction.setText("Map Funktion");
	
		final TextArea textAreaMap = new TextArea();
		textAreaMap.getStyleClass().add("map-reduce-area");
		textAreaMap.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		        document.setMapFunction(textAreaMap.getText());
		    }
		});
		paneMapFunction.setContent(textAreaMap);
		wrapperMapReduceFunctions.getChildren().add(paneMapFunction);
	
		// Textarea for reduce function
		TitledPane paneReduceFunction = new TitledPane();
		paneReduceFunction.setText("Reduce Funktion");
	
		final TextArea textAreaReduce = new TextArea();
		textAreaReduce.getStyleClass().add("map-reduce-area");
		textAreaReduce.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		        document.setReduceFunction(textAreaReduce.getText());
		    }
		});
		
		paneReduceFunction.setContent(textAreaReduce);
		wrapperMapReduceFunctions.getChildren().add(paneReduceFunction);
		
		loadTextAreas(textAreaMap, textAreaReduce, doc);
		
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
