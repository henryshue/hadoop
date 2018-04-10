package cn.i.search.core.elasticsearch.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import cn.i.search.core.api.QueryService;
import cn.i.search.core.api.exception.CloseClientFailException;
import cn.i.search.core.api.exception.InitClientFailException;
import cn.i.search.core.api.index.IndexService;
import cn.i.search.core.api.sugget.SuggestService;
import cn.i.search.core.common.CommonUtil;
import cn.i.search.core.elasticsearch.index.NativeIndexService;
import cn.i.search.core.elasticsearch.query.NativeQueryService;
import cn.i.search.core.elasticsearch.suggest.NativeSuggestService;
import cn.i.search.core.elasticsearch.utils.Constants;

public class NativeClient implements Serializable, InitializingBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937302597850250647L;
	private static TransportClient client;
	private static final String KEY_CLUSTER_NAME = "cluster.name";
	private static final String KEY_CLIENT_TRANSPORT_SNIFF = "client.transport.sniff";

	private String clusterName;
	private boolean clientTransportSniff;

	private String hosts;
	private static final Logger logger = LoggerFactory.getLogger(NativeClient.class);

	public NativeClient(String clusterName, String hosts) {
		this.clusterName = clusterName;
		this.clientTransportSniff = false;
		this.hosts = hosts;
	}

	public NativeClient(String clusterName, boolean clientTransportSniff, String hosts) {
		this.clusterName = clusterName;
		this.clientTransportSniff = clientTransportSniff;
		this.hosts = hosts;
	}

	private void init() throws InitClientFailException {
		if (CommonUtil.isNullOrEmpty(clusterName)) {
			logger.error("core包报错，构建连接失败，参数clusterName没有配置");
			throw new InitClientFailException("构建连接失败，参数clusterName没有设置");
		}
		if (CommonUtil.isNullOrEmpty(hosts)) {
			logger.error("core包报错，构建连接失败，参数hosts没有配置");
			throw new InitClientFailException("构建连接失败，参数hosts没有设置");
		}
		if (null == client) {
			PreBuiltTransportClient preClient = null;
			try {
				Settings settings = Settings.builder().put(KEY_CLUSTER_NAME, clusterName)
						.put(KEY_CLIENT_TRANSPORT_SNIFF, clientTransportSniff).build();
				preClient = new PreBuiltTransportClient(settings);
				String[] hostStrs = getHostsByStrs(hosts);
				for (String host : hostStrs) {
					String[] holeHost = host.split(":");
					preClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(holeHost[0]),
							Integer.valueOf(holeHost[1])));
				}
				client = preClient;
			} catch (NumberFormatException e) {
				logger.error("core包报错：数字格式化异常，构建连接失败", e);
				throw new InitClientFailException(e);
			} catch (UnknownHostException e) {
				logger.error("core包报错：未知的主机，构建连接失败", e);
				throw new InitClientFailException(e);
			} catch (Exception e) {
				logger.error("core包报错：其他异常，构建连接失败", e);
				throw new InitClientFailException(e);
			}
		}
	}

	private String[] getHostsByStrs(String hosts) {
		if (!CommonUtil.isNullOrEmpty(hosts)) {
			return hosts.split(Constants.CODE_HOSTS_SPLIT);
		}
		return null;
	}

	public void close() throws CloseClientFailException {
		if (null != client) {
			try {
				client.close();
				client = null;
			} catch (Exception e) {
				logger.error("core包报错，关闭连接失败", e);
				throw new CloseClientFailException(e);
			}
		}
	}

	public IndexService getIndexService() {
		return new NativeIndexService(client);
	}

	public QueryService getQueryService() {
		return new NativeQueryService(client);
	}

	public SuggestService getSuggestService() {
		return new NativeSuggestService(client);
	}

	public static TransportClient getClient() {
		return client;
	}

	public static void setClient(TransportClient client) {
		NativeClient.client = client;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public void afterPropertiesSet() throws Exception {
		init();

	}
}
