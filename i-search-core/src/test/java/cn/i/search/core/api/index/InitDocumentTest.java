package cn.i.search.core.api.index;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import cn.i.search.core.common.ConfigUtil;
import cn.i.search.core.elasticsearch.index.NativeIndexService;

public class InitDocumentTest {

	private static final String CLUSTER_NAME = "es_cluster";
	private static final String INDEXNAME = "announcement";
	private TransportClient client;
	private NativeIndexService nativeIndexService;

	@Before
	public void before() throws UnknownHostException {
		// 初始化客户端
		Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
		client = new PreBuiltTransportClient(settings);
		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.137.200"), 9300));
		nativeIndexService = new NativeIndexService(client);
	}

	public void initIndex() {
		if (!nativeIndexService.isIndexExists(INDEXNAME)) {
			String filePath = InitDocumentTest.class.getClassLoader().getResource("mappings/document.search.xml")
					.getFile();
			String settingsStr = ConfigUtil.getStringForXmlFile(filePath, ConfigUtil.SETTINGS);
			String mappings = ConfigUtil.getStringForXmlFile(filePath, ConfigUtil.MAPPINGS);

			System.out.println("filePath=" + filePath);
			System.out.println("settingsStr=" + settingsStr);
			System.out.println("mappings=" + mappings);

			nativeIndexService.createIndex(INDEXNAME, settingsStr, mappings);

		}
		// 放入file
		initDocument();
	}

	@Test
	public void initDocument() {
		String filePath = InitDocumentTest.class.getClassLoader().getResource("mappings/document.search.xml").getFile();
		String path = "D:/bigdata/studing/ElasticSearch/test/docs/遇到的问题点.docx";
		File file = new File(path);
		nativeIndexService.insertLocalFile(INDEXNAME, filePath, file);
	}

	@Test
	public void initDocumentAndSearch() {
		// 初始化一个index
		initIndex();
	}

	@Test
	// 已ok
	public void matchAllQuery() throws Exception {
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
		SearchResponse response = client.prepareSearch("article").setQuery(queryBuilder).get();
		for (SearchHit searchHit : response.getHits()) {
			println(searchHit);
		}
	}

	/**
	 * 输出结果SearchResponse
	 * 
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	public static void println(SearchHit searchHit) {
		System.err.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
		System.err.println("docId : " + searchHit.docId() + "\n" + "getId : " + searchHit.getId() + "\n" + "getIndex : "
				+ searchHit.getIndex() + "\n" + "getScore : " + searchHit.getScore() + "\n" + "getSourceAsString : "
				+ searchHit.getSourceAsString() + "\n" + "getType : " + searchHit.getType() + "\n" + "getVersion : "
				+ searchHit.getVersion() + "\n" + "fieldsOrNull : " + searchHit.fieldsOrNull() + "\n"
				+ "getExplanation : " + searchHit.getExplanation() + "\n" + "getFields : " + searchHit.getFields()
				+ "\n" + "highlightFields : " + searchHit.highlightFields() + "\n" + "hasSource : "
				+ searchHit.hasSource());
	}

}
