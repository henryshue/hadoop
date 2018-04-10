package cn.i.search.core.common;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.i.search.core.api.exception.SearchEngineRuntimeException;

public class ConfigUtil {

	static Map<String, Object> configMap;
	public static final String SETTINGS = "settings";
	public static final String MAPPINGS = "mappings";
	public static final String PIPELINESETTINGS = "pipelineSettings";

	public ConfigUtil() {

	}

	/**
	 * 获取配置文件内容
	 * 
	 * @param filePath
	 *            文件地址
	 * @param id
	 *            XML节点ID，规范只有两个ID，ConfigUtil.SETTINGS ConfigUtil.MAPPINGS
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public static String getStringForXmlFile(String filePath, String id) {
		String result = null;
		List<Element> childElements = null;
		// 配置的Map
		if (configMap == null) {
			configMap = new HashMap<>();
		}
		try {
			// 如果已经存在，就直接输出
			if (!"".equals(CommonUtil.objectToString(configMap.get(filePath + "@" + id)))) {
				return CommonUtil.objectToString(configMap.get(filePath + "@" + id));
			}
			// 如果已经读过这个文件，就在数据中找
			if (configMap.get(filePath) != null) {
				childElements = (List<Element>) configMap.get(filePath);
			}
			// 如果没有就只好去找文件了
			if (childElements == null) {
				SAXReader reader = new SAXReader();
				File file = new File(filePath);
				Document document = reader.read(file);
				Element root = document.getRootElement();
				childElements = root.elements();
				// 放入Map
				configMap.put(filePath, childElements);
			}
			// 取出数据
			for (Element element : childElements) {
				if (element.attributeValue("id").equals(id)) {
					result = element.getText();
					// 放入Map
					configMap.put(filePath + "@" + id, result);
				}
			}
		} catch (DocumentException e) {
			throw new SearchEngineRuntimeException("常量工具getStringForXmlFile方法报错", e);
		}
		return result;
	}

}
