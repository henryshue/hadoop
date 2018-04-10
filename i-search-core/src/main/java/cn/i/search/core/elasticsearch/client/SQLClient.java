package cn.i.search.core.elasticsearch.client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.druid.pool.ElasticSearchConnection;

import cn.i.search.core.api.QueryService;
import cn.i.search.core.api.exception.CloseClientFailException;
import cn.i.search.core.api.exception.InitClientFailException;
import cn.i.search.core.elasticsearch.query.SQLQueryService;
import cn.i.search.core.elasticsearch.utils.ConnectionUtil;

public class SQLClient implements Serializable, InitializingBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9038730661990856013L;
	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
	private static Connection conn;
	private String host;
	private int initialize;

	public SQLClient(String host) throws InitClientFailException {
		this.host = host;
		this.initialize = 1;
	}

	public SQLClient(String host, int initialize) {
		this.host = host;
		this.initialize = initialize;
	}

	private void init() throws InitClientFailException {
		if (null == conn) {
			try {
				String url = String.format("jdbc:elasticsearch://%s/", host);
				Properties properties = new Properties();
				properties.put("url", url);
				conn = ConnectionUtil.getDataSource(properties).getConnection();
			} catch (Exception e) {
				logger.error("core报错，构建SQL CLIENT报错");
				throw new InitClientFailException(e);
			}
		}
	}

	public boolean isClosed() {
		if (null != conn) {
			try {
				return conn.isClosed();
			} catch (SQLException e) {
				logger.error("获取SQL Client关闭信息失败");
			}
		}
		return true;
	}

	public void close() throws CloseClientFailException {
		if (null != conn) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				throw new CloseClientFailException(e);
			}
		}
	}

	public QueryService getQueryService() {
		return new SQLQueryService(conn);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getInitialize() {
		return initialize;
	}

	public void setInitialize(int initialize) {
		this.initialize = initialize;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	class SQLConnection extends ElasticSearchConnection {
		public SQLConnection(String arg0) {
			super(arg0);
		}
	}

}
