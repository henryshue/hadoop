package cn.i.search.core.api.query;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TransportClientTest {

	private TransportClient client;
	private final static String article = "announcement";
	private final static String content = "announcement";

	@Before
	public void getClient() throws Exception {
		// 设置集群名称
		Settings settings = Settings.builder().put("cluster.name", "es_cluster").build();// 集群名
		// 创建client
		client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.137.200"), 9300));
	}

	/**
	 * 创建索引并添加映射
	 * 
	 * @throws IOException
	 */
	// ok
	@Test
	public void CreateIndexAndMapping() throws Exception {

		CreateIndexRequestBuilder cib = client.admin().indices().prepareCreate(article);
		XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("properties")
				// 设置之定义字段
				.startObject("author").field("type", "string")
				// 设置数据类型
				.endObject().startObject("title").field("type", "string").endObject().startObject("content")
				.field("type", "string").endObject().startObject("price").field("type", "string").endObject()
				.startObject("view").field("type", "string").endObject().startObject("tag").field("type", "string")
				.endObject().startObject("date").field("type", "date") // 设置Date类型
				.field("format", "yyyy-MM-dd HH:mm:ss") // 设置Date的格式
				.endObject().endObject().endObject();
		cib.addMapping(content, mapping);

		CreateIndexResponse res = cib.execute().actionGet();

		System.out.println("----------添加映射成功----------" + res.isAcknowledged());
	}

	/**
	 * 创建索引并添加文档
	 * 
	 * @throws Exception
	 */
	@Test
	// ok
	public void addIndexAndDocument() throws Exception {

		Date time = new Date();

		IndexResponse response = client.prepareIndex(article, content)
				.setSource(XContentFactory.jsonBuilder().startObject().field("id", "200").field("author", "hfxu")
						.field("title", "192.138.1.2").field("content", "这是烹饪有关的书籍").field("price", "30")
						.field("view", "100").field("tag", "a,b,c,d,e,f")
						.field("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)).endObject())
				.get();
		System.out.println("添加索引成功,版本号：" + response.getVersion());
	}

	/**
	 * bulkRequest
	 * 
	 * @throws Exception
	 */
	@Test
	// ok
	public void bulkRequest() throws Exception {
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		Date time = new Date();

		// either use client#prepare, or use Requests# to directly build
		// index/delete requests
		bulkRequest.add(client.prepareIndex(article, content, "199")
				.setSource(XContentFactory.jsonBuilder().startObject().field("id", "199").field("author", "fendo")
						.field("title", "BULK").field("content", "这是BULK有关的书籍").field("price", "40")
						.field("view", "300").field("tag", "a,b,c")
						.field("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)).endObject()));

		bulkRequest.add(client.prepareIndex(article, content, "101")
				.setSource(XContentFactory.jsonBuilder().startObject().field("id", "101").field("author", "fendo")
						.field("title", "ACKSE").field("content", "这是ACKSE有关的书籍").field("price", "50")
						.field("view", "200").field("tag", "a,b,c")
						.field("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)).endObject()));

		BulkResponse bulkResponse = bulkRequest.get();
		if (bulkResponse.hasFailures()) {
			// process failures by iterating through each bulk response item
			// System.out.println(bulkResponse.getTook());
		}
		System.out.println("创建成功!!!");
	}

	/**
	 * 查询article索引下的所有数据 <a href=
	 * 'https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-query-dsl-match
	 * - a l l - q u e r y . h t m l ' >
	 * 
	 * @throws Exception
	 */
	@Test
	public void matchAllQuery() throws Exception {
		QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
		SearchResponse response = client.prepareSearch(article).setQuery(queryBuilder).get();
		for (SearchHit searchHit : response.getHits()) {
			println(searchHit);
		}
		System.out.println("查询完成");
	}

	/**
	 * 查询article索引下的articledate的所有数据
	 * 
	 * @throws Exception
	 */
	@Test
	public void searchmethod1() throws Exception {
		SearchResponse response = client.prepareSearch(article).setTypes(content).get();
		println(response);
		for (SearchHit searchHit : response.getHits()) {
			println(searchHit);
		}
		System.out.println("查询完成");
	}

	/**
	 * 输出结果SearchResponse
	 * 
	 * @param response
	 */
	public static void println(SearchResponse response) {
		System.err.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
		System.err.println("getFailedShards : " + response.getFailedShards() + "\n" + "getNumReducePhases : "
				+ response.getNumReducePhases() + "\n" + "getScrollId : " + response.getScrollId() + "\n"
				+ "getTookInMillis : " + response.getTookInMillis() + "\n" + "getTotalShards : "
				+ response.getTotalShards() + "\n" + "getAggregations : " + response.getAggregations() + "\n"
				+ "getProfileResults : " + response.getProfileResults() + "\n" + "getShardFailures : "
				+ response.getShardFailures() + "\n" + "getSuggest : " + response.getSuggest() + "\n" + "getTook : "
				+ response.getTook() + "\n" + "isTerminatedEarly : " + response.isTerminatedEarly() + "\n"
				+ "isTimedOut : " + response.isTimedOut() + "\n" + "remoteAddress : " + response.remoteAddress() + "\n"
				+ "status : " + response.status() + "\n" + "getHits : " + response.getHits());
	}

	/**
	 * 输出结果SearchResponse
	 * 
	 * @param response
	 */
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
	
	   @Test
		public void testConfigforhenry() {
			System.out.println("start");
			ApplicationContext factory = new ClassPathXmlApplicationContext("spring-search.xml");
			//ApplicationContext  factory = new ClassPathXmlApplicationContext("file:D:/study/JAVA/workbench/i-search-core/src/main/resources/spring/spring-search.xml");
			
			//IndexService indexSerivce = (IndexService)factory.getBean("indexService");		

			System.out.println("ok");
		}

}
