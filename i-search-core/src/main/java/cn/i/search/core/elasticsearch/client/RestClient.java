package cn.i.search.core.elasticsearch.client;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.i.search.core.api.exception.CloseClientFailException;
import cn.i.search.core.api.exception.InitClientFailException;

public class RestClient implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8893011020313421049L;

	private static org.elasticsearch.client.RestClient client;
	private static final String TYPE_HTTP = "http";
	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	private String[] hosts;

	public RestClient(String[] hosts) throws InitClientFailException {
		this.hosts = hosts;
		init();
	}

	private void init() throws InitClientFailException {
		if (null == client) {
			try {
				HttpHost[] httpHosts = new HttpHost[hosts.length];
				for (int i = 0; i < hosts.length; i++) {
					String[] hostStr = hosts[i].split(":");
					httpHosts[i] = new HttpHost(hostStr[0], Integer.valueOf(hostStr[i]), TYPE_HTTP);
				}
				client = org.elasticsearch.client.RestClient.builder(httpHosts).build();
			} catch (Exception e) {
				logger.error("core报错，构建REST CLIENT报错");
				throw new InitClientFailException(e);
			}
		}
	}

	public boolean isClosed() {
		return null == client;
	}

	public void close() throws CloseClientFailException {
		if (null != client) {
			try {
				client.close();
				client = null;
			} catch (IOException e) {
				logger.error("core报错，关闭REST CLIENT报错");
				throw new CloseClientFailException(e);
			}
		}
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}

}
