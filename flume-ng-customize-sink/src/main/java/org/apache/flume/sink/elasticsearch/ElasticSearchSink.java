/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.flume.sink.elasticsearch;

//import static org.apache.flume.sink.elasticsearch.ElasticSearchEventSerializer.charset;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.BATCH_SIZE;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.CLUSTER_NAME;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.DEFAULT_CLUSTER_NAME;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.DEFAULT_INDEX_NAME;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.DEFAULT_INDEX_NAME_BUILDER_CLASS;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.DEFAULT_INDEX_TYPE;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.HOSTNAMES;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.INDEX_NAME;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.INDEX_NAME_BUILDER;
import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.INDEX_TYPE;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

/**
 * A sink which reads events from a channel and writes them to ElasticSearch
 * based on the work done by https://github.com/Aconex/elasticflume.git.
 * </p>
 * 
 * This sink supports batch reading of events from the channel and writing them
 * to ElasticSearch.
 * </p>
 * 
 * Indexes will be rolled daily using the format 'indexname-YYYY-MM-dd' to allow
 * easier management of the index
 * </p>
 * 
 * This sink must be configured with with mandatory parameters detailed in
 * {@link ElasticSearchSinkConstants}
 * </p>
 * It is recommended as a secondary step the ElasticSearch indexes are optimized
 * for the specified serializer. This is not handled by the sink but is
 * typically done by deploying a config template alongside the ElasticSearch
 * deploy
 * </p>
 * 
 * @see http
 *      ://www.elasticsearch.org/guide/reference/api/admin-indices-templates.
 *      html
 */
public class ElasticSearchSink extends AbstractSink implements Configurable {

  private static final Logger logger = LoggerFactory.getLogger(ElasticSearchSink.class);
  public static final Charset charset = Charset.defaultCharset();

  private boolean isLocal = false;
  private final CounterGroup counterGroup = new CounterGroup();

  private static final int defaultBatchSize = 100;
  private int batchSize = defaultBatchSize;
  private String clusterName = DEFAULT_CLUSTER_NAME;
  private String indexName = DEFAULT_INDEX_NAME;
  private String indexType = DEFAULT_INDEX_TYPE;
  private String[] serverAddresses = null;
  private TransportClient esClient = null;
  private BulkRequestBuilder bulkRequestBuilder;
  private IndexNameBuilder indexNameBuilder;
  private SinkCounter sinkCounter;

  /**
   * Create an {@link ElasticSearchSink} configured using the supplied
   * configuration
   */
  public ElasticSearchSink() {
    this(false);
  }

  /**
   * Create an {@link ElasticSearchSink}
   * </p>
   * 
   * @param isLocal
   *          If <tt>true</tt> sink will be configured to only talk to an
   *          ElasticSearch instance hosted in the same JVM, should always be
   *          false is production
   * 
   */
  @VisibleForTesting
  ElasticSearchSink(boolean isLocal) {
    this.isLocal = isLocal;
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    Channel channel = getChannel();
    Transaction txn = channel.getTransaction();
    Event event = null;
    try {
      txn.begin();
      int count;
      for (count = 0; count < batchSize; ++count) {
        event = channel.take();
        if (event == null) {
          break;
        }
        if (event.getBody().length == 0) {
          continue;
        }
        analyzeLog(bulkRequestBuilder, event);
      }

      if (count <= 0) {
        sinkCounter.incrementBatchEmptyCount();
        counterGroup.incrementAndGet("channel.underflow");
        status = Status.BACKOFF;
      } else {
        if (count < batchSize) {
          sinkCounter.incrementBatchUnderflowCount();
          status = Status.BACKOFF;
        } else {
          sinkCounter.incrementBatchCompleteCount();
        }

        sinkCounter.addToEventDrainAttemptCount(count);
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        bulkRequestBuilder.request().requests().clear();
        if (bulkResponse.hasFailures()) {
          System.out.println("bulkResponse failure");
        }
      }
      txn.commit();
      sinkCounter.addToEventDrainSuccessCount(count);
      counterGroup.incrementAndGet("transaction.success");
    } catch (

    Throwable ex) {
      if (txn != null) {
        txn.rollback();
      }
      ex.printStackTrace();
    } finally {
      if (txn != null) {
        txn.close();
      }
    }
    return status;
  }

  @Override
  public void configure(Context context) {
    if (!isLocal) {
      if (StringUtils.isNotBlank(context.getString(HOSTNAMES))) {
        serverAddresses = StringUtils.deleteWhitespace(context.getString(HOSTNAMES)).split(",");
      }
      Preconditions.checkState(serverAddresses != null && serverAddresses.length > 0,
          "Missing Param:" + HOSTNAMES);
    }

    if (StringUtils.isNotBlank(context.getString(INDEX_NAME))) {
      this.indexName = context.getString(INDEX_NAME);
    }

    if (StringUtils.isNotBlank(context.getString(INDEX_TYPE))) {
      this.indexType = context.getString(INDEX_TYPE);
    }

    if (StringUtils.isNotBlank(context.getString(CLUSTER_NAME))) {
      this.clusterName = context.getString(CLUSTER_NAME);
    }

    if (sinkCounter == null) {
      sinkCounter = new SinkCounter(getName());
    }

    String indexNameBuilderClass = DEFAULT_INDEX_NAME_BUILDER_CLASS;
    if (StringUtils.isNotBlank(context.getString(INDEX_NAME_BUILDER))) {
      indexNameBuilderClass = context.getString(INDEX_NAME_BUILDER);
    }

    Context indexnameBuilderContext = new Context();

    try {
      @SuppressWarnings("unchecked")
      Class<? extends IndexNameBuilder> clazz = (Class<? extends IndexNameBuilder>) Class
          .forName(indexNameBuilderClass);
      indexNameBuilder = clazz.newInstance();
      indexnameBuilderContext.put(INDEX_NAME, indexName);
      indexNameBuilder.configure(indexnameBuilderContext);
    } catch (Exception e) {
      logger.error("Could not instantiate index name builder.", e);
      Throwables.propagate(e);
    }

    if (sinkCounter == null) {
      sinkCounter = new SinkCounter(getName());
    }

    Preconditions.checkState(StringUtils.isNotBlank(indexName), "Missing Param:" + INDEX_NAME);
    Preconditions.checkState(StringUtils.isNotBlank(indexType), "Missing Param:" + INDEX_TYPE);
    Preconditions.checkState(StringUtils.isNotBlank(clusterName), "Missing Param:" + CLUSTER_NAME);
    Preconditions.checkState(batchSize >= 1, BATCH_SIZE + " must be greater than 0");
  }

  @Override
  public synchronized void start() {
    // 设置集群名称
    System.out.println("clusterName1:" + clusterName + "|"
        + Settings.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    Builder builder = Settings.builder();
    Settings settings = builder.put("cluster.name", clusterName).build();

    sinkCounter.start();
    // 创建client
    for (String esIpTcpport : serverAddresses) {
      String[] hostPort = esIpTcpport.trim().split(":");
      try {
        if (null == esClient) {
          System.out.println("#####countGroup="
              + CounterGroup.class.getProtectionDomain().getCodeSource().getLocation().getFile());
          System.out.println("clusterName2:" + clusterName + "|" + PreBuiltTransportClient.class
              .getProtectionDomain().getCodeSource().getLocation().getFile());

          esClient = new PreBuiltTransportClient(settings).addTransportAddress(
              new InetSocketTransportAddress(InetAddress.getByName(hostPort[0]),
                  Integer.parseInt(hostPort[1])));
        } else {
          esClient = esClient.addTransportAddress(new InetSocketTransportAddress(
              InetAddress.getByName(hostPort[0]), Integer.parseInt(hostPort[1])));
        }
        sinkCounter.incrementConnectionCreatedCount();
      } catch (UnknownHostException e) {
        sinkCounter.incrementConnectionFailedCount();
        e.printStackTrace();
        if (esClient != null) {
          esClient.close();
          sinkCounter.incrementConnectionClosedCount();
        }
      }
    }
    // 批量
    bulkRequestBuilder = esClient.prepareBulk();
    super.start();
  }

  @Override
  public synchronized void stop() {
    logger.info("ElasticSearch sink {} stopping");
    if (esClient != null) {
      esClient.close();
    }
    sinkCounter.incrementConnectionClosedCount();
    sinkCounter.stop();
    super.stop();
  }

  public void analyzeLog(BulkRequestBuilder bulkRequestBuilder, Event event) {
    System.out.println("Start analyzeLog...");

    Map<String, Object> headers = Maps.newHashMap(event.getHeaders());
    byte[] body = event.getBody();

    headers.put("content", new String(body));

    String jsonHeader = "";
    for (String key : headers.keySet()) {
      jsonHeader = jsonHeader + key + "=" + new String(headers.get(key).toString());
    }
    System.out.println("Received Header:" + jsonHeader);

    String json = new Gson().toJson(headers);
    System.out.println("json=" + json);

    String logType = "pop3";
    try {
      logType = new String(headers.get("logType").toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Calendar calendar = Calendar.getInstance();
    Date date = new Date();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONDAY);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    logType = logType.replace("%y", String.valueOf(year));
    logType = logType.replace("%m", String.format("%02d", month + 1));
    logType = logType.replace("%d", String.format("%02d", day));

    // indexName = String.format("%s%s%tF", logType,
    // ElasticSearchSinkConstants.CODE_INDEX_SPLIT,
    // new Date());
    indexName = logType;
    indexType = logType;
    String uuid = UUID.randomUUID().toString().replaceAll("-", "");

    try {
      System.out.println("start add to builder...");
      @SuppressWarnings("deprecation")
      IndexRequestBuilder indexRequest = esClient.prepareIndex(indexName, indexType)
          // 指定不重复的ID
          .setSource(json).setId(String.valueOf(uuid));
      // 添加到builder中
      bulkRequestBuilder.add(indexRequest);
      System.out.println("add to builder ok");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
