package cn.i.search.core.api.query.entity;

import java.util.Map;

public class Field {
	/* 字段名称 */
	private String name;
	/* 权重 */
	private int boost;
	/* 值 */
	private Map<String, Object> value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBoost() {
		return boost;
	}

	public void setBoost(int boost) {
		this.boost = boost;
	}

	public Map<String, Object> getValue() {
		return value;
	}

	public void setValue(Map<String, Object> value) {
		this.value = value;
	}

	public String getFieldAndBoostStr() {
		if (boost == 0) {
			return this.name;
		}
		return this.name + "^" + boost;
	}

}
