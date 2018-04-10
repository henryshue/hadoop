package cn.i.search.core.api.sugget.entity;

public class SuggestParams {

	private String[] index;
	private String fields;
	private String userInput;
	private String suggestQueryName;
	private int limit;

	public String[] getIndex() {
		return index;
	}

	public void setIndex(String[] index) {
		this.index = index;
	}

	public void setIndex(String index) {
		this.index = new String[] { index };
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}

	public String getSuggestQueryName() {
		return suggestQueryName;
	}

	public void setSuggestQueryName(String suggestQueryName) {
		this.suggestQueryName = suggestQueryName;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
