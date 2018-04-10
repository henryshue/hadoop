package cn.i.search.core.elasticsearch.utils;

import cn.i.search.core.common.CommonUtil;

public class IndexUtil {

	/**
	 * 根据索引名称，获取类别
	 * 
	 * @param indexName
	 * @return
	 */
	public static String getType(String indexName) {
		if (!CommonUtil.isNullOrEmpty(indexName)) {
			String[] indexInfos = indexName.split(Constants.CODE_INDEX_SPLIT);
			if (indexInfos.length > 1) {
				return CommonUtil.nullToEmpty(indexInfos[1]);
			}
		}
		return indexName;
	}

	/**
	 * 根据索引名称，获取别名
	 * 
	 * @param indexName
	 * @return
	 */
	public static String getAlias(String indexName) {
		if (!CommonUtil.isNullOrEmpty(indexName)) {
			String[] indexInfos = indexName.split(Constants.CODE_INDEX_SPLIT);
			if (indexInfos.length > 1) {
				return new StringBuilder().append(CommonUtil.nullToEmpty(indexInfos[0]))
						.append(Constants.CODE_INDEX_SPLIT).append(CommonUtil.nullToEmpty(indexInfos[1])).toString();
			}
		}
		return indexName;
	}

}
