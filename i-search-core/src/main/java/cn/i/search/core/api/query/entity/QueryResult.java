package cn.i.search.core.api.query.entity;

import java.util.List;
import java.util.Map;

public interface QueryResult {

	public long getTotal();

	public long getTook();

	public String getIndex();

	public double getMaxScore();

	public List<Map<String, Object>> getDatas();

}
