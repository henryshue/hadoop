package cn.i.search.core.api.query.entity;

public class QueryParams {
	private int start;
	private int limit;
	private String index;
	private String type;
	private String sort;
	private String userInput;
	private Field[] fields;
	private Filter[] filters;
	private HightLightField[] hightlightFields;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public Filter[] getFilters() {
		return filters;
	}

	public void setFilters(Filter[] filters) {
		this.filters = filters;
	}

	public HightLightField[] getHightlightFields() {
		return hightlightFields;
	}

	public void setHightlightFields(HightLightField[] hightlightFields) {
		this.hightlightFields = hightlightFields;
	}

}
