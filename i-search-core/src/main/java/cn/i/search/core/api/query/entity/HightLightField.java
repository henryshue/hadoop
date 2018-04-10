package cn.i.search.core.api.query.entity;

public class HightLightField {

	private String field;
	private int textLength;
	private int segmentSize;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}

	public int getSegmentSize() {
		return segmentSize;
	}

	public void setSegmentSize(int segmentSize) {
		this.segmentSize = segmentSize;
	}

}
