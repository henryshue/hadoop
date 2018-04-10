package cn.i.search.core.elasticsearch.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.PreparedStatement;

import cn.i.search.core.api.QueryService;
import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.exception.UnsupportedMethodException;
import cn.i.search.core.api.query.entity.QueryParams;
import cn.i.search.core.api.query.entity.QueryResult;
import cn.i.search.core.common.CommonUtil;

public class SQLQueryService implements QueryService {

	private Connection conn;
	private static final Logger logger = LoggerFactory.getLogger(SQLQueryService.class);

	public SQLQueryService(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public QueryResult search(QueryParams params) throws SearchException {
		return (QueryResult) new UnsupportedMethodException("使用SQLClient时，不支持使用这个方法");
	}

	@Override
	public QueryResult search(String sql) throws SearchException {
		QueryResultSet qrs = new QueryResultSet();
		Map<String, Object> record = null;
		List<Map<String, Object>> datas = null;
		try (PreparedStatement ps = (PreparedStatement) conn.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {
			ResultSetMetaData rsmeta = rs.getMetaData();
			int columnCount = rsmeta.getColumnCount();
			datas = new ArrayList<>();
			while (rs.next()) {
				record = new HashMap<>();
				for (int i = 0; i < columnCount; i++) {
					String column = rsmeta.getColumnName(i);
					Object value = rs.getObject(column);
					if (null == value) {
						value = CommonUtil.objectToString(value);
					}
					record.put(column, value);
				}
				datas.add(record);
			}
			qrs.setDatas(datas);
		} catch (SQLException e) {
			logger.error("core报错，SQL Client查询异常");
			throw new SearchException("搜索异常", e);
		}
		return qrs;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

}
