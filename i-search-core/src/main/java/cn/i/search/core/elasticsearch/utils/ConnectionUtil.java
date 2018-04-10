package cn.i.search.core.elasticsearch.utils;

import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.ElasticSearchDruidDataSourceFactory;

public class ConnectionUtil {

	private static DruidDataSource dds;

	private ConnectionUtil() {

	}

	public static DruidDataSource getDataSource(Properties properties) throws Exception {
		if (null == dds) {
			synchronized (ConnectionUtil.class) {
				if (null == dds) {
					dds = (DruidDataSource) ElasticSearchDruidDataSourceFactory.createDataSource(properties);
				}
			}
		}
		return dds;
	}
}
