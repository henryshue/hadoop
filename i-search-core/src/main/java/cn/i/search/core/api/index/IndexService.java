package cn.i.search.core.api.index;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IndexService {

	/**
	 * 创建一个新的空索引
	 * 
	 * @param indexName
	 */
	public boolean createIndex(String indexName);

	/**
	 * 创建索引同时，添加索引设置
	 * 
	 * @param indexName
	 * @param settings
	 */
	public boolean createIndex(String indexName, String settings);

	/**
	 * 创建索引
	 * 
	 * @param indexName
	 * @param settings
	 * @param mappings
	 */
	public boolean createIndex(String indexName, String settings, String mappings);

	/**
	 * 删除一个索引
	 * 
	 * @param indexName
	 */
	public boolean deleteIndex(String indexName);

	/**
	 * 更新索引设置
	 * 
	 * @param indexName
	 * @param settings
	 */
	public boolean updateIndex(String indexName, Map<String, Object> settings);

	/**
	 * 判断索引是否存在
	 * 
	 * @param indexName
	 */
	public boolean isIndexExists(String indexName);

	/**
	 * 初始化映射
	 * 
	 * @param indexName
	 * @param schemaJsonStr
	 */
	public boolean initSchema(String indexName, String schemaJsonStr);

	/**
	 * 增加数据
	 * 
	 * @param indexName
	 * @param dataJsonStr
	 */
	public void insertData(String indexName, String dataJsonStr);

	/**
	 * 增加数据
	 * 
	 * @param indexName
	 * @param id指定Id
	 * @param dataJsonStr
	 */
	public void insertData(String indexName, String id, String dataJsonStr);

	/**
	 * 根据ID删除
	 * 
	 * @param indexName
	 * @param id
	 * @return
	 */
	public void deleteData(String indexName, String id);

	/**
	 * 更新数据，如果数据不存在，将会新增一条
	 * 
	 * @param indexName
	 * @param id
	 * @param dataJsonStr
	 */
	public void updateData(String indexName, String id, String dataJsonStr);

	/**
	 * 根据ID获取数据
	 * 
	 * @param indexName
	 * @param id
	 * @param className
	 * @return
	 */
	public <T> T getData(String indexName, String id, Class<T> className);

	/**
	 * 批量提交
	 * 
	 * @param indexName索引名称
	 * @param idName集合的对象中，作为ID字段名称
	 * @param list对象集合（data）
	 */
	public void batchAddData(String indexName, String idName, Collection<?> list);

	/**
	 * 手动刷新
	 * 
	 * @param index索引名称
	 */
	public void reflush(String index);

	/**
	 * 索引本地文档
	 * 
	 * @param indexName索引名称
	 * @param configFileName配置文件路径
	 * @param file目标文件
	 */
	public void insertLocalFile(String indexName, String configFileName, File file);

	/**
	 * 索引本地文档
	 * 
	 * @param indexName索引名称
	 * @param configFileName配置文件路径
	 * @param dataObject目标文件解析之后
	 */
	public void insertLocalFile(String indexName, String configFileName, Map<String, Object> dataObject);

	/**
	 * 批量索引本地文档
	 * 
	 * @param indexName索引名称
	 * @param configFileName配置文件路径
	 * @param files目标文件集
	 */
	public void insertLocalFiles(String indexName, String configFileName, List<File> files);

	/**
	 * 批量索引本地文档
	 * 
	 * @param indexName索引名称
	 * @param configFileName配置文件路径
	 * @param dataStrs
	 */
	public void insertLocalFileObjects(String indexName, String configFileName, List<Map<String, Object>> dataStrs);

}
