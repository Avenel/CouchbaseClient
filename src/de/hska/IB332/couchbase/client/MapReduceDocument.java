package de.hska.IB332.couchbase.client;

import java.io.File;
import java.io.Serializable;

public class MapReduceDocument implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String mapFunction;
	String reduceFunction;
	String viewName;
	String designDocName;
	File targetFile;
	
	
	public MapReduceDocument (String designDocName, String viewName) {
		this.viewName = viewName;
		this.designDocName = designDocName;
		
		this.mapFunction = "function(doc, meta) {\n}";
		this.reduceFunction = "function(key, values, rereduce) {\n}";
	}
	
	public MapReduceDocument(String mapFunction, String reduceFunction,
			String viewName, String designDocName) {
		this.mapFunction = mapFunction;
		this.reduceFunction = reduceFunction;
		this.viewName = viewName;
		this.designDocName = designDocName;
	}
	public String getMapFunction() {
		return mapFunction;
	}
	public void setMapFunction(String mapFunction) {
		this.mapFunction = mapFunction;
	}
	public String getReduceFunction() {
		return reduceFunction;
	}
	public void setReduceFunction(String reduceFunction) {
		this.reduceFunction = reduceFunction;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getDesignDocName() {
		return designDocName;
	}
	public void setDesignDocName(String designDocName) {
		this.designDocName = designDocName;
	}

	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
	}
	
	
}
