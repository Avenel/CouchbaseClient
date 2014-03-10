package de.hska.IB332.couchbase.client;

public class CouchbaseResultRow {
	private String key;
	private String result;

	public CouchbaseResultRow(String key, String result) {
		super();
		this.key = key;
		this.result = result;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
