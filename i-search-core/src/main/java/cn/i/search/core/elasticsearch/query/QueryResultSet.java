package cn.i.search.core.elasticsearch.query;

import java.util.List;
import java.util.Map;

import cn.i.search.core.api.query.entity.QueryResult;

public class QueryResultSet implements QueryResult {

	private String id;
	private String index;
	private long total;
	private double maxScore;
	private List<Map<String, Object>> datas;
	private List<Map<String, Object>> highlightText;
	private long took;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}

	public List<Map<String, Object>> getDatas() {
		return datas;
	}

	public void setDatas(List<Map<String, Object>> datas) {
		this.datas = datas;
	}

	public List<Map<String, Object>> getHighlightText() {
		return highlightText;
	}

	public void setHighlightText(List<Map<String, Object>> highlightText) {
		this.highlightText = highlightText;
	}

	public long getTook() {
		return took;
	}

	public void setTook(long took) {
		this.took = took;
	}

}
