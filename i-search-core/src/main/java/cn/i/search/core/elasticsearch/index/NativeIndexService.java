package cn.i.search.core.elasticsearch.index;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.i.search.core.api.index.IndexService;
import cn.i.search.core.common.CommonUtil;
import cn.i.search.core.common.ConfigUtil;
import cn.i.search.core.elasticsearch.utils.Constants;
import cn.i.search.core.elasticsearch.utils.FileUtil;
import cn.i.search.core.elasticsearch.utils.IndexUtil;

public class NativeIndexService implements IndexService {

  private static final Logger logger = LoggerFactory.getLogger(NativeIndexService.class);
  private TransportClient client;
  private BulkProcessor bulkProcessor;
  /* 批量提交 每次提交记录条数 */
  private int batchCommitLength;
  /* 批量提交 每次提交记录的大小 */
  private int batchCommitSize;
  /* 批量提交 并发处理的线程 */
  private int batchConcurrendThread;
  /* 批量提交 失败重试的时间间隔 */
  private int batchTimeInterval;
  /* 批量提交 失败重试的次数 */
  private int batchRetryTimes;

  public NativeIndexService() {

  }

  public NativeIndexService(TransportClient client) {
    this.client = client;
  }

  @Override
  public boolean createIndex(String indexName) {
    return createIndex(indexName, null, null);
  }

  @Override
  public boolean createIndex(String indexName, String settings) {
    return createIndex(indexName, settings, null);
  }

  @Override
  public boolean createIndex(String indexName, String settings, String mappings) {
    if (CommonUtil.isNullOrEmpty(indexName)) {
      return false;
    }
    String alias = IndexUtil.getAlias(indexName);
    String type = IndexUtil.getAlias(indexName);
    if (indexName.equals(alias)) {
      indexName = String.format("%s%s%tF", indexName, Constants.CODE_INDEX_SPLIT, new Date());
    }
    CreateIndexRequestBuilder createRequestBuilder = client.admin().indices()
        .prepareCreate(indexName).addAlias(new Alias(alias));
    if (!CommonUtil.isNullOrEmpty(settings)) {
      createRequestBuilder.setSettings(settings, XContentType.JSON);
    }
    if (!("").equals(CommonUtil.nullToEmpty(mappings))) {
      createRequestBuilder.addMapping(type, mappings, XContentType.JSON);
    }
    return createRequestBuilder.get().isAcknowledged();
  }

  @Override
  public boolean deleteIndex(String indexName) {
    return client.admin().indices().prepareDelete(indexName).get().isAcknowledged();
  }

  @Override
  public boolean updateIndex(String indexName, Map<String, Object> settings) {
    return client.admin().indices().prepareUpdateSettings(indexName).setSettings(settings).get()
        .isAcknowledged();
  }

  @Override
  public boolean isIndexExists(String indexName) {
    return client.admin().indices().prepareExists(indexName).get().isExists();
  }

  @Override
  public boolean initSchema(String indexName, String schemaJsonStr) {
    return client.admin().indices().preparePutMapping(indexName)
        .setType(IndexUtil.getType(indexName))
        .setSource(new BytesArray(schemaJsonStr), XContentType.JSON).get().isAcknowledged();
  }

  @Override
  public void insertData(String indexName, String dataJsonStr) {
    client.prepareIndex(indexName, IndexUtil.getType(indexName))
        .setSource(new BytesArray(dataJsonStr), XContentType.JSON).get();
  }

  @Override
  public void insertData(String indexName, String id, String dataJsonStr) {
    client.prepareIndex(indexName, IndexUtil.getType(indexName)).setId(id)
        .setSource(new BytesArray(dataJsonStr), XContentType.JSON).get();
  }

  @Override
  public void insertLocalFile(String indexName, String configFileName, File file) {
    if (!isExistsPipeLine(Constants.DEFAULT_INGEST_ATTACHMENT)) {
      insertPipeLine(configFileName, Constants.DEFAULT_INGEST_ATTACHMENT);
    }
    Map<String, Object> data = FileUtil.readFileToBytes(file);
    client.prepareIndex(indexName, IndexUtil.getType(indexName))
        .setPipeline(Constants.DEFAULT_INGEST_ATTACHMENT)
        .setSource(new BytesArray(CommonUtil.toJsonString(data)), XContentType.JSON).get();
  }

  @Override
  public void insertLocalFile(String indexName, String configFileName,
      Map<String, Object> dataObject) {
    if (!isExistsPipeLine(Constants.DEFAULT_INGEST_ATTACHMENT)) {
      insertPipeLine(configFileName, Constants.DEFAULT_INGEST_ATTACHMENT);
    }
    client.prepareIndex(indexName, IndexUtil.getType(indexName))
        .setPipeline(Constants.DEFAULT_INGEST_ATTACHMENT)
        .setSource(new BytesArray(CommonUtil.toJsonString(dataObject)), XContentType.JSON).get();
  }

  @Override
  public void insertLocalFiles(String indexName, String configFileName, List<File> files) {
    if (!isExistsPipeLine(Constants.DEFAULT_INGEST_ATTACHMENT)) {
      insertPipeLine(configFileName, Constants.DEFAULT_INGEST_ATTACHMENT);
    }
    if (null == bulkProcessor) {
      initBulkProcessor();
    }
    IndexRequest indexRequestBuilder = null;
    for (File f : files) {
      Map<String, Object> data = FileUtil.readFileToBytes(f);
      indexRequestBuilder = new IndexRequest(indexName, IndexUtil.getType(indexName));
      indexRequestBuilder.source(new BytesArray(CommonUtil.toJsonString(data)), XContentType.JSON)
          .setPipeline(Constants.DEFAULT_INGEST_ATTACHMENT);
      bulkProcessor.add(indexRequestBuilder);
    }
    bulkProcessor.flush();
    closeBulkProcessor();
  }

  @Override
  public void insertLocalFileObjects(String indexName, String configFileName,
      List<Map<String, Object>> dataStrs) {
    if (!isExistsPipeLine(Constants.DEFAULT_INGEST_ATTACHMENT)) {
      insertPipeLine(configFileName, Constants.DEFAULT_INGEST_ATTACHMENT);
    }
    if (null == bulkProcessor) {
      initBulkProcessor();
    }
    IndexRequest indexRequestBuilder = null;
    for (Map<String, Object> f : dataStrs) {
      indexRequestBuilder = new IndexRequest(indexName, IndexUtil.getType(indexName));
      indexRequestBuilder.source(new BytesArray(CommonUtil.toJsonString(f)), XContentType.JSON)
          .setPipeline(Constants.DEFAULT_INGEST_ATTACHMENT);
      bulkProcessor.add(indexRequestBuilder);
    }
    bulkProcessor.flush();
    closeBulkProcessor();
  }

  public boolean isExistsPipeLine(String pipeLineId) {
    boolean result;
    result = client.admin().cluster().prepareGetPipeline(pipeLineId).get().isFound();
    return result;
  }

  public boolean insertPipeLine(String fileName, String pipeLineId) {
    boolean result = false;
    if (!CommonUtil.isNullOrEmpty(pipeLineId) && !CommonUtil.isNullOrEmpty(fileName)) {
      String pipelineSettings = ConfigUtil.getStringForXmlFile(fileName,
          ConfigUtil.PIPELINESETTINGS);
      result = client
          .admin().cluster().preparePutPipeline(pipeLineId,
              new BytesArray(pipelineSettings.getBytes()), XContentType.JSON)
          .get().isAcknowledged();
    }
    return result;
  }

  @Override
  public void deleteData(String indexName, String id) {
    client.prepareDelete(indexName, IndexUtil.getType(indexName), id).get();
  }

  @Override
  public void updateData(String indexName, String id, String dataJsonStr) {
    client.prepareUpdate(indexName, IndexUtil.getType(indexName), id)
        .setDoc(dataJsonStr, XContentType.JSON).get();
  }

  @SuppressWarnings("unchecked")
  public <T> T getData(String indexName, String id, Class<T> className) {
    GetResponse resultSet = client.prepareGet(indexName, IndexUtil.getType(indexName), id)
        .setOperationThreaded(false).get();
    Map<String, Object> result = resultSet.getSource();
    if (null != result) {
      result.put(Constants.KEY_ID, resultSet.getId());
    }
    return (T) CommonUtil.transMap2Bean(result, className);
  }

  @Override
  public void batchAddData(String indexName, String idName, Collection<?> list) {
    if (null == bulkProcessor) {
      initBulkProcessor();
    }
    IndexRequest indexRequestBuilder = null;
    String id = "";
    for (Object obj : list) {
      indexRequestBuilder = new IndexRequest(indexName, IndexUtil.getType(indexName));
      if (!CommonUtil.isNullOrEmpty(idName)) {
        id = CommonUtil.objectToString(CommonUtil.getFieldValueByName(idName, obj));
        if (!CommonUtil.isNullOrEmpty(id)) {
          indexRequestBuilder.id(id);
        }
      }
      indexRequestBuilder.source(new BytesArray(CommonUtil.toJsonString(obj)), XContentType.JSON);
      bulkProcessor.add(indexRequestBuilder);
    }
    bulkProcessor.flush();
    closeBulkProcessor();
  }

  private void initBulkProcessor() {
    if (batchCommitLength == 0) {
      batchCommitLength = Constants.BATCH_LENGTH;
    }
    if (batchCommitSize == 0) {
      batchCommitLength = Constants.BATCH_SIZE;
    }
    org.elasticsearch.action.bulk.BulkProcessor.Builder batchRequestBuilder = BulkProcessor
        .builder(client, new BulkProcessor.Listener() {

          @Override
          public void beforeBulk(long executionId, BulkRequest request) {

          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

          }
          // 每次批量新增条数
        }).setBulkActions(batchCommitLength)
        .setBulkSize(new ByteSizeValue(batchCommitSize, ByteSizeUnit.MB))
        // 每次批量多大数据量
        .setConcurrentRequests(batchConcurrendThread);
    // 一次性并发多少请求
    if (batchTimeInterval != 0 && batchRetryTimes != 0) {
      batchRequestBuilder.setBackoffPolicy(BackoffPolicy
          .exponentialBackoff(TimeValue.timeValueMillis(batchTimeInterval), batchRetryTimes));
    }
    bulkProcessor = batchRequestBuilder.build();
  }

  private void closeBulkProcessor() {
    try {
      if (bulkProcessor != null) {
        bulkProcessor.awaitClose(3, TimeUnit.MINUTES);
      }
      bulkProcessor = null;
    } catch (InterruptedException e) {
      logger.error("bulkProcessor close fail！", e);
    }
  }

  @Override
  public void reflush(String index) {
    if (CommonUtil.isNullOrEmpty(index)) {
      return;
    }
    client.admin().indices().prepareRefresh(index).get();
  }

  public TransportClient getClient() {
    return client;
  }

  public void setClient(TransportClient client) {
    this.client = client;
  }

  public int getBatchCommitLength() {
    return batchCommitLength;
  }

  public void setBatchCommitLength(int batchCommitLength) {
    this.batchCommitLength = batchCommitLength;
  }

  public int getBatchCommitSize() {
    return batchCommitSize;
  }

  public void setBatchCommitSize(int batchCommitSize) {
    this.batchCommitSize = batchCommitSize;
  }

  public int getBatchConcurrendThread() {
    return batchConcurrendThread;
  }

  public void setBatchConcurrendThread(int batchConcurrendThread) {
    this.batchConcurrendThread = batchConcurrendThread;
  }

  public int getBatchTimeInterval() {
    return batchTimeInterval;
  }

  public void setBatchTimeInterval(int batchTimeInterval) {
    this.batchTimeInterval = batchTimeInterval;
  }

  public int getBatchRetryTimes() {
    return batchRetryTimes;
  }

  public void setBatchRetryTimes(int batchRetryTimes) {
    this.batchRetryTimes = batchRetryTimes;
  }

}
