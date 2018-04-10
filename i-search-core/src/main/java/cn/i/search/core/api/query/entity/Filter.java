package cn.i.search.core.api.query.entity;

public class Filter {
	/*字段*/
	private Field field;
	/*操作符*/
	private OperateCode operate;
	/*值*/
	private Object value;
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public OperateCode getOperate() {
		return operate;
	}
	public void setOperate(OperateCode operate) {
		this.operate = operate;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	

}
