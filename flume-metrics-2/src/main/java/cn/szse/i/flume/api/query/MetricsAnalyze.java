package cn.szse.i.flume.api.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import cn.szse.i.flume.domain.MetricsBean;

public class MetricsAnalyze {

  private static final Logger logger = LoggerFactory.getLogger(MetricsAnalyze.class);

  private List<MetricsBean>[] metricsListA;
  private List<MetricsBean>[] metricsListB;

  // 节点类型，第1层sink到kafka，第2层sink到elasticsearch和hdfs
  int nodeType = 0;
  // 默认第一层flume有3个节点，第二层flume有1个节点
  int nodeA = 3;
  int nodeB = 1;
  // 监控12个端口，数据展示共9列
  static final int rowcount = 12;
  static final int columncount = 9;
  // 分12个端口监控12种不同类型的日志
  String[] logTypeArray;
  String[] portArray;
  // 最后返回的结果，用数组保存起来
  private String[][][] arrayA = new String[nodeA][rowcount][columncount];
  private String[][][] arrayB = new String[nodeB][rowcount * 2][columncount];
  // 上一次的统计结果
  private String[][][] old_arrayA = new String[nodeA][rowcount][2];
  private String[][][] old_arrayB = new String[nodeB][rowcount * 2][2];

  public List<MetricsBean> getMetricsListA(int nodeNo) {
    metricsListA[nodeNo].clear();
    for (int i = 0; i < rowcount; i++) {
      MetricsBean bean = new MetricsBean();
      bean.setAgentname(arrayA[nodeNo][i][0]);
      bean.setListenin(arrayA[nodeNo][i][1]);
      bean.setAllin(Long.parseLong(arrayA[nodeNo][i][2]));
      bean.setAllout(Long.parseLong(arrayA[nodeNo][i][3]));
      bean.setInrate(Long.parseLong(arrayA[nodeNo][i][4]));
      bean.setOutrate(Long.parseLong(arrayA[nodeNo][i][5]));
      bean.setCapacity(Long.parseLong(arrayA[nodeNo][i][6]));
      bean.setChannelsize(Long.parseLong(arrayA[nodeNo][i][7]));
      bean.setSinkto(arrayA[nodeNo][i][8]);
      metricsListA[nodeNo].add(bean);
    }
    return metricsListA[nodeNo];
  }

  public List<MetricsBean> getMetricsListB(int nodeNo) {
    metricsListB[nodeNo].clear();

    for (int i = 0; i < rowcount * 2; i++) {
      MetricsBean bean = new MetricsBean();
      bean.setAgentname(arrayB[nodeNo][i][0]);
      bean.setListenin(arrayB[nodeNo][i][1]);
      bean.setAllin(Long.parseLong(arrayB[nodeNo][i][2]));
      bean.setAllout(Long.parseLong(arrayB[nodeNo][i][3]));
      bean.setInrate(Long.parseLong(arrayB[nodeNo][i][4]));
      bean.setOutrate(Long.parseLong(arrayB[nodeNo][i][5]));
      bean.setCapacity(Long.parseLong(arrayB[nodeNo][i][6]));
      bean.setChannelsize(Long.parseLong(arrayB[nodeNo][i][7]));
      bean.setSinkto(arrayB[nodeNo][i][8]);
      metricsListB[nodeNo].add(bean);
    }
    return metricsListB[nodeNo];
  }

  public void run_analyze(String ipAddress, String logTypeStr, String portStr, String delayPeriod) {
    String[] hostname = ipAddress.split(",");

    logTypeArray = logTypeStr.split(",");
    portArray = portStr.split(",");

    for (int i = 0; i < nodeA; i++) {
      for (int j = 0; j < rowcount; j++) {
        arrayA[i][j][0] = logTypeArray[j];
        arrayA[i][j][1] = portArray[j];
        arrayA[i][j][2] = "0";
        arrayA[i][j][3] = "0";
        arrayA[i][j][4] = "0";
        arrayA[i][j][5] = "0";
        arrayA[i][j][6] = "0";
        arrayA[i][j][7] = "0";
        arrayA[i][j][8] = "-";

        old_arrayA[i][j][0] = "0";
        old_arrayA[i][j][1] = "0";
      }
    }

    for (int i = 0; i < nodeB; i++) {
      for (int j = 0; j < rowcount; j++) {
        arrayB[i][j * 2][0] = logTypeArray[j];
        arrayB[i][j * 2][1] = portArray[j];
        arrayB[i][j * 2][2] = "0";
        arrayB[i][j * 2][3] = "0";
        arrayB[i][j * 2][4] = "0";
        arrayB[i][j * 2][5] = "0";
        arrayB[i][j * 2][6] = "0";
        arrayB[i][j * 2][7] = "0";
        arrayB[i][j * 2][8] = "-";

        old_arrayB[i][j * 2][0] = "0";
        old_arrayB[i][j * 2][1] = "0";
      }

      for (int j = 0; j < rowcount; j++) {
        arrayB[i][j * 2 + 1][0] = logTypeArray[j];
        arrayB[i][j * 2 + 1][1] = portArray[j];
        arrayB[i][j * 2 + 1][2] = "0";
        arrayB[i][j * 2 + 1][3] = "0";
        arrayB[i][j * 2 + 1][4] = "0";
        arrayB[i][j * 2 + 1][5] = "0";
        arrayB[i][j * 2 + 1][6] = "0";
        arrayB[i][j * 2 + 1][7] = "0";
        arrayB[i][j * 2 + 1][8] = "-";

        old_arrayB[i][j * 2 + 1][0] = "0";
        old_arrayB[i][j * 2 + 1][1] = "0";
      }
    }
    metricsListA = new ArrayList[nodeA];
    for (int i = 0; i < nodeA; i++) {
      metricsListA[i] = new ArrayList<MetricsBean>();
    }
    metricsListB = new ArrayList[nodeB];
    for (int i = 0; i < nodeB; i++) {
      metricsListB[i] = new ArrayList<MetricsBean>();
    }
    // 分别启动第1层和第2层的监控进程
    for (int i = 0; i < nodeA; i++) {
      new Thread(new ThreadMetrics(1, i, hostname[i], logTypeStr, portStr, delayPeriod)).start();
    }
    for (int i = 0; i < nodeB; i++) {
      new Thread(new ThreadMetrics(2, i, hostname[i], logTypeStr, portStr, delayPeriod)).start();
    }
  }

  class ThreadMetrics implements Runnable {
    private int nodeType;
    private int nodeNo;
    private String hostName;
    private String logTypeStr;
    private String portStr;
    private String delayPeriod;

    public ThreadMetrics(int nodeType, int nodeNo, String hostName, String logTypeStr,
        String portStr, String delayPeriod) {
      this.nodeType = nodeType;
      this.nodeNo = nodeNo;
      this.hostName = hostName;
      this.logTypeStr = logTypeStr;
      this.portStr = portStr;
      this.delayPeriod = delayPeriod;
    }

    private Map getOneMetrics(String metricsURL) {
      logger.info("get metrics:" + metricsURL);
      CloseableHttpClient httpClient = HttpClients.createDefault();

      HttpGet request = new HttpGet(metricsURL);
      request.setHeader("Connection", "close");
      int statusCode = 0;
      try {
        HttpResponse response = httpClient.execute(request);

        if ((statusCode = response.getStatusLine().getStatusCode()) == HttpStatus.SC_OK) {
          String json = EntityUtils.toString(response.getEntity());
          Map map = new Gson().fromJson(json, Map.class);
          return map;

        } else {
          logger.error("get metrics failed!url:{},statusCode:{}", metricsURL, statusCode);
        }
      } catch (Exception e) {
        logger.error("get metrics failed! url:{}, statusCode:{}", metricsURL, statusCode);
      } finally {
        try {
          request.releaseConnection();
          httpClient.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    private void addMetricsA(Map metricsMap, int nodeNo, int row, String agentName,
        String listenin) {
      LinkedTreeMap channel = null;
      int getSuccess = 0;
      try {
        // 注意：本机测试是CHANNEL.ch1，内网是CHANNEL.channel1
        channel = (LinkedTreeMap) metricsMap.get("CHANNEL.channel1");
        if (null != channel) {
          getSuccess = 1;
        }
      } catch (Exception e) {
        logger.error("addMetricsA error{}", row);
      }

      if (1 == getSuccess) {
        try {
          String takeCount = String.valueOf(channel.get("EventTakeSuccessCount"));
          String putCount = String.valueOf(channel.get("EventPutSuccessCount"));

          String inRate = "0";
          String outRate = "0";
          try {
            inRate = String
                .valueOf(((Long.parseLong(takeCount) - Long.parseLong(old_arrayA[nodeNo][row][0]))
                    * 1000 / Long.parseLong(delayPeriod)));
            outRate = String
                .valueOf(((Long.parseLong(putCount) - Long.parseLong(old_arrayA[nodeNo][row][1]))
                    * 1000 / Long.parseLong(delayPeriod)));
          } catch (Exception e) {

          }

          arrayA[nodeNo][row][0] = agentName;
          arrayA[nodeNo][row][1] = listenin;
          arrayA[nodeNo][row][2] = takeCount;
          arrayA[nodeNo][row][3] = putCount;
          arrayA[nodeNo][row][4] = inRate;
          arrayA[nodeNo][row][5] = outRate;
          arrayA[nodeNo][row][6] = String.valueOf(channel.get("ChannelCapacity"));
          arrayA[nodeNo][row][7] = String.valueOf(channel.get("ChannelSize"));
          arrayA[nodeNo][row][8] = "kafka";

          old_arrayA[nodeNo][row][0] = takeCount;
          old_arrayA[nodeNo][row][1] = putCount;

        } catch (Exception e) {
          logger.error("addMetricsA error{}", row);
        }
      } else {
        arrayA[nodeNo][row][0] = agentName;
        arrayA[nodeNo][row][1] = listenin;
        arrayA[nodeNo][row][2] = "0";
        arrayA[nodeNo][row][3] = "0";
        arrayA[nodeNo][row][4] = "0";
        arrayA[nodeNo][row][5] = "0";
        arrayA[nodeNo][row][6] = "0";
        arrayA[nodeNo][row][7] = "0";
        arrayA[nodeNo][row][8] = "-";

        old_arrayA[nodeNo][row][0] = "0";
        old_arrayA[nodeNo][row][1] = "0";

      }
    }

    private void addMetricsB(Map metricsMap, int nodeNo, int row, String agentName,
        String listenin) {
      LinkedTreeMap channel = null;
      int getSuccess = 0;
      try {
        // 注意：本机测试是CHANNEL.ch1，内网是CHANNEL.channel1
        channel = (LinkedTreeMap) metricsMap.get("CHANNEL.channel1");
        if (null != channel) {
          getSuccess = 1;
        }
      } catch (Exception e) {
        logger.error("addMetrics1 error{}", row);
      }
      if (1 == getSuccess) {
        try {
          // 注意：本机测试是CHANNEL.ch1，内网是CHANNEL.channel1
          channel = (LinkedTreeMap) metricsMap.get("CHANNEL.channel1");

          String takeCount = String.valueOf(channel.get("EventTakeSuccessCount"));
          String putCount = String.valueOf(channel.get("EventPutSuccessCount"));

          String inRate = "0";
          String outRate = "0";
          try {
            inRate = String.valueOf(
                ((Long.parseLong(takeCount) - Long.parseLong(old_arrayB[nodeNo][row * 2][0])) * 1000
                    / Long.parseLong(delayPeriod)));
            outRate = String.valueOf(
                ((Long.parseLong(putCount) - Long.parseLong(old_arrayB[nodeNo][row * 2][1])) * 1000
                    / Long.parseLong(delayPeriod)));
          } catch (Exception e) {

          }

          arrayB[nodeNo][row * 2][0] = agentName;
          arrayB[nodeNo][row * 2][1] = listenin;
          arrayB[nodeNo][row * 2][2] = takeCount;
          arrayB[nodeNo][row * 2][3] = putCount;
          arrayB[nodeNo][row * 2][4] = inRate;
          arrayB[nodeNo][row * 2][5] = outRate;
          arrayB[nodeNo][row * 2][6] = String.valueOf(channel.get("ChannelCapacity"));
          arrayB[nodeNo][row * 2][7] = String.valueOf(channel.get("ChannelSize"));
          arrayB[nodeNo][row * 2][8] = "ElasticSearch";

          old_arrayB[nodeNo][row * 2][0] = takeCount;
          old_arrayB[nodeNo][row * 2][1] = putCount;
        } catch (Exception e) {
          logger.error("addMetricsB error{}", row * 2);
        }
      } else {
        arrayB[nodeNo][row * 2][0] = agentName;
        arrayB[nodeNo][row * 2][1] = listenin;
        arrayB[nodeNo][row * 2][2] = "0";
        arrayB[nodeNo][row * 2][3] = "0";
        arrayB[nodeNo][row * 2][4] = "0";
        arrayB[nodeNo][row * 2][5] = "0";
        arrayB[nodeNo][row * 2][6] = "0";
        arrayB[nodeNo][row * 2][7] = "0";
        arrayB[nodeNo][row * 2][8] = "-";

        old_arrayB[nodeNo][row * 2][0] = "0";
        old_arrayB[nodeNo][row * 2][1] = "0";

      }
      channel = null;
      getSuccess = 0;
      try {
        // 注意：本机测试是CHANNEL.ch1，内网是CHANNEL.channel1
        channel = (LinkedTreeMap) metricsMap.get("CHANNEL.channel2");
        if (null != channel) {
          getSuccess = 1;
        }
      } catch (Exception e) {
        logger.error("addMetrics1 error{}", row);
      }

      if (1 == getSuccess) {
        try {
          String takeCount = String.valueOf(channel.get("EventTakeSuccessCount"));
          String putCount = String.valueOf(channel.get("EventPutSuccessCount"));

          String inRate = "0";
          String outRate = "0";
          try {
            inRate = String.valueOf(
                ((Long.parseLong(takeCount) - Long.parseLong(old_arrayB[nodeNo][row * 2 + 1][0]))
                    * 1000 / Long.parseLong(delayPeriod)));
            outRate = String.valueOf(
                ((Long.parseLong(putCount) - Long.parseLong(old_arrayB[nodeNo][row * 2 + 1][1]))
                    * 1000 / Long.parseLong(delayPeriod)));
          } catch (Exception e) {

          }
          arrayB[nodeNo][row * 2 + 1][0] = agentName;
          arrayB[nodeNo][row * 2 + 1][1] = listenin;
          arrayB[nodeNo][row * 2 + 1][2] = takeCount;
          arrayB[nodeNo][row * 2 + 1][3] = putCount;
          arrayB[nodeNo][row * 2 + 1][4] = inRate;
          arrayB[nodeNo][row * 2 + 1][5] = outRate;
          arrayB[nodeNo][row * 2 + 1][6] = String.valueOf(channel.get("ChannelCapacity"));
          arrayB[nodeNo][row * 2 + 1][7] = String.valueOf(channel.get("ChannelSize"));
          arrayB[nodeNo][row * 2 + 1][8] = "HDFS";

          old_arrayB[nodeNo][row * 2 + 1][0] = takeCount;
          old_arrayB[nodeNo][row * 2 + 1][1] = putCount;
        } catch (Exception e) {

        }
      } else {
        arrayB[nodeNo][row * 2 + 1][0] = agentName;
        arrayB[nodeNo][row * 2 + 1][1] = listenin;
        arrayB[nodeNo][row * 2 + 1][2] = "0";
        arrayB[nodeNo][row * 2 + 1][3] = "0";
        arrayB[nodeNo][row * 2 + 1][4] = "0";
        arrayB[nodeNo][row * 2 + 1][5] = "0";
        arrayB[nodeNo][row * 2 + 1][6] = "0";
        arrayB[nodeNo][row * 2 + 1][7] = "0";
        arrayB[nodeNo][row * 2 + 1][8] = "-";

        old_arrayB[nodeNo][row * 2 + 1][0] = "0";
        old_arrayB[nodeNo][row * 2 + 1][1] = "0";

      }
    }

    public void run() {
      while (true) {
        try {
          for (int i = 0; i < logTypeArray.length; i++) {
            String metricsURL = "http://" + hostName + ":" + portArray[i] + "/metrics";
            Map metricsMap = getOneMetrics(metricsURL);

            if (1 == nodeType) {
              addMetricsA(metricsMap, nodeNo, i, logTypeArray[i], portArray[i]);
            } else if (2 == nodeType) {
              addMetricsB(metricsMap, nodeNo, i, logTypeArray[i], portArray[i]);
            }
          }
          Thread.sleep(Integer.parseInt(delayPeriod));
        } catch (InterruptedException e) {
          System.out.println("线程运行中断异常");
        }
      }
    }
  }

}
