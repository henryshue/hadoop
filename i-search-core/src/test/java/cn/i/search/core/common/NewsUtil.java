package cn.i.search.core.common;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.i.search.core.api.exception.SearchEngineRuntimeException;

public class NewsUtil {

	public static final String CONTENT = "content";
	public static final String TITLE = "title";
	public static final String TIME = "updated";

	@SuppressWarnings("unchecked")
	public static String getElement(File file, String name) {
		SAXReader reader = new SAXReader();
		Document document;
		Element root;
		StringBuilder result = new StringBuilder("");
		try {
			document = reader.read(file);
			root = document.getRootElement();
			List<Element> childElements = root.elements(name);
			for (Element element : childElements) {
				result.append(element.getText());
			}
		} catch (DocumentException e) {
			throw new SearchEngineRuntimeException("解析新闻xml文件方法报错", e);
		}
		return result.toString();
	}

	@SuppressWarnings("unchecked")
	public static String getLabel(File file) {
		SAXReader reader = new SAXReader();
		Document document;
		Element root;
		StringBuilder result = new StringBuilder("");
		try {
			document = reader.read(file);
			root = document.getRootElement();
			List<Element> childElements = root.elements("codes");
			root = childElements.get(0);
			childElements = root.elements("code");
			for (Element element : childElements) {
				result.append(element.getText());
			}
		} catch (DocumentException e) {
			throw new SearchEngineRuntimeException("解析新闻xml文件方法报错", e);
		}
		return result.toString();
	}

}
