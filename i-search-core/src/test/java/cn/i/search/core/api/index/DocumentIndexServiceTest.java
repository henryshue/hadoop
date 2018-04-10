package cn.i.search.core.api.index;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.i.search.core.api.QueryService;
import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.query.entity.Field;
import cn.i.search.core.api.query.entity.Filter;
import cn.i.search.core.api.query.entity.OperateCode;
import cn.i.search.core.api.query.entity.QueryParams;
import cn.i.search.core.api.query.entity.QueryResult;
import cn.i.search.core.common.BaseTestCase;
import cn.i.search.core.common.ConfigUtil;
import cn.i.search.core.elasticsearch.index.NativeIndexService;
import cn.i.search.core.elasticsearch.utils.Constants;
import cn.i.search.core.elasticsearch.utils.FileUtil;

public class DocumentIndexServiceTest extends BaseTestCase {

	@Autowired
	private NativeIndexService indexService;
	@Autowired
	private QueryService searchService;

	@Before
	public void before() {
		String indexName = "announcement";
		if (!indexService.isIndexExists(indexName)) {
			String configFileName = DocumentIndexServiceTest.class.getClassLoader()
					.getResource("mappings/document.search.xml").getFile();
			String settings = ConfigUtil.getStringForXmlFile(configFileName, ConfigUtil.SETTINGS);
			String mappings = ConfigUtil.getStringForXmlFile(configFileName, ConfigUtil.MAPPINGS);
			System.out.println(settings);
			System.out.println(mappings);
		}
	}

	// @Before
	public void testCreateDocumentIndex() {
		String indexName = "gonggao";
		if (indexService.isIndexExists(indexName)) {
			indexService.deleteIndex(indexName);
		}
		String configFileName = DocumentIndexServiceTest.class.getClassLoader()
				.getResource("mappings/document.search.xml").getFile();
		String settings = ConfigUtil.getStringForXmlFile(configFileName, ConfigUtil.SETTINGS);
		String mappings = ConfigUtil.getStringForXmlFile(configFileName, ConfigUtil.MAPPINGS);
		indexService.createIndex(indexName, settings, mappings);
	}

	public void testIngestAttchment() {
		String filePath = DocumentIndexServiceTest.class.getClassLoader().getResource("mappings/document.search.xml")
				.getFile();
		indexService.insertPipeLine(filePath, Constants.DEFAULT_INGEST_ATTACHMENT);
	}

	@Test
	public void testIndexDocument() {
		String indexName = "gonggao";

		String configFileName = InitDocumentTest.class.getClassLoader().getResource("mappings/document.search.xml")
				.getFile();
		String filePath = "D:/bigdata/studing/ElasticSearch/test/docs/遇到的问题点.docx";
		List<File> files = FileUtil.getLocalFilesByPath(filePath);
		indexService.insertLocalFiles(indexName, configFileName, files);
	}

	@Test
	//程序调试ok，条件不满足要求未查询到数据
	public void testSearchDocument() throws SearchException, ParseException {
		QueryParams params = new QueryParams();
		Field fileName = new Field();
		fileName.setName("fileName");
		Field content = new Field();
		content.setName("attachment.content");
		Field date = new Field();
		date.setName("attachment.date");
		params.setFields(new Field[] { fileName, content });
		Filter ageFilter = new Filter();
		ageFilter.setField(date);
		ageFilter.setOperate(OperateCode.gte);
		Date dateValue = new SimpleDateFormat("yyyy-MM-dd").parse("2017-11-16");
		ageFilter.setValue(dateValue.getTime());
		params.setFilters(new Filter[] { ageFilter });
		params.setIndex("announcement");
		// params.setSort("+age");
		params.setUserInput("docx");
		params.setLimit(10);
		params.setStart(0);
		QueryResult queryResult = searchService.search(params);
		List<Map<String, Object>> datas = queryResult.getDatas();
		System.out.println("搜索结果长度：" + (datas == null ? "0" : datas.size() + ""));
		for (Map<String, Object> data : datas) {
			System.out.println(String.format("文件名称：%s%n文件内容：%s%n文件日期：%tF%n匹配得分:%f%n", data.get(fileName.getName()),
					data.get(content.getName()), data.get(date.getName()), data.get("score")));
		}
	}
}
