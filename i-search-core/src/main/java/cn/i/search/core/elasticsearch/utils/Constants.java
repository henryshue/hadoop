package cn.i.search.core.elasticsearch.utils;

public interface Constants {

	/** 索引名称分隔符 */
	public static final String CODE_INDEX_SPLIT = "_";
	/** host分隔符 */
	public static final String CODE_HOSTS_SPLIT = ";";
	/** 排序分隔符 */
	public static final String CODE_SORTS_SPLIT = ",";
	/** 排序-正序符号 */
	public static final String CODE_SORTS_INVERTED = "+";
	/** 排序-倒序符号 */
	public static final String CODE_SORTS_REINVERTED = "-";

	/** id的key值 */
	public static final String KEY_ID = "id";
	/** 批量提交-每批提交的记录条数 */
	public static final int BATCH_LENGTH = 1000;
	/** 批量提交-每批提交的数据大小15MB */
	public static final int BATCH_SIZE = 15;

	/** 查询-默认查询条数 */
	public static final int SIZE_DEFAULT_LIMIT = 10;

	/** 查询-0条数 */
	public static final int SIZE_EMPTY = 0;
	/** 建议-默认建议条数 */
	public static final int SIZE_SUGGEST_DEFAULT = 10;
	/** 默认接收附件 */
	public static final String DEFAULT_INGEST_ATTACHMENT = "pipeline_attachment";

}
