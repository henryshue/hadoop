package cn.i.search.core.elasticsearch.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.i.search.core.api.QueryService;
import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.exception.UnsupportedMethodException;
import cn.i.search.core.api.query.entity.Field;
import cn.i.search.core.api.query.entity.Filter;
import cn.i.search.core.api.query.entity.HightLightField;
import cn.i.search.core.api.query.entity.OperateCode;
import cn.i.search.core.api.query.entity.QueryParams;
import cn.i.search.core.api.query.entity.QueryResult;
import cn.i.search.core.common.CommonUtil;
import cn.i.search.core.elasticsearch.utils.Constants;
import cn.i.search.core.elasticsearch.utils.IndexUtil;

public class NativeQueryService implements QueryService {

	private TransportClient client;
	private static final Logger logger = LoggerFactory.getLogger(NativeQueryService.class);

	private static final String KEY_SCORE = "score";

	public NativeQueryService(TransportClient client) {
		super();
		this.client = client;
	}

	@Override
	public QueryResult search(QueryParams params) throws SearchException {
		QueryResultSet result = null;
		if (null != params) {
			// 封装请求体
			SearchRequestBuilder searchRequest = buildRequst(params);
			// 封装查询器
			QueryBuilder queryBuilder = buildQuery(params);
			// 注入查询器
			searchRequest.setQuery(queryBuilder);

			logger.info("searchRequest:" + searchRequest);
			// 执行查询
			SearchResponse response = searchRequest.get();
			// 封装返回体
			result = buildResponse(response);
		}
		return result;
	}

	private SearchRequestBuilder buildRequst(QueryParams params) {
		SearchRequestBuilder searchRequest = null;
		if (!CommonUtil.isNullOrEmpty(params.getIndex())) {
			if (CommonUtil.isNullOrEmpty(params.getType())) {
				params.setType(IndexUtil.getType(params.getIndex()));
			}
			searchRequest = client.prepareSearch(params.getIndex()).setTypes(params.getType());
		}
		if (0 == params.getLimit()) {
			params.setLimit(Constants.SIZE_DEFAULT_LIMIT);
		}
		if (null != searchRequest) {
			searchRequest.setFrom(params.getStart()).setSize(params.getLimit());
		}
		if (null != params.getSort()) {
			String[] sorts = params.getSort().split(Constants.CODE_SORTS_SPLIT);
			for (String sort : sorts) {
				if (CommonUtil.nullToEmpty(sort).startsWith(Constants.CODE_SORTS_INVERTED)) {
					searchRequest.addSort(CommonUtil.nullToEmpty(sort).substring(1), SortOrder.ASC);
				} else if (CommonUtil.nullToEmpty(sort).startsWith(Constants.CODE_SORTS_REINVERTED)) {
					searchRequest.addSort(CommonUtil.nullToEmpty(sort).substring(1), SortOrder.DESC);
				} else {
					searchRequest.addSort(CommonUtil.nullToEmpty(sort), SortOrder.ASC);
				}
			}
		}
		if (null != params.getHightlightFields()) {
			HighlightBuilder highlightBuilder = new HighlightBuilder();
			for (HightLightField field : params.getHightlightFields()) {
				highlightBuilder.field(CommonUtil.objectToString(field.getField()),
						HighlightBuilder.DEFAULT_FRAGMENT_CHAR_SIZE);
			}
			highlightBuilder.preTags("<font style='color:red'>");
			highlightBuilder.postTags("</font>");
			highlightBuilder.requireFieldMatch(false);
			searchRequest.highlighter(highlightBuilder);
		}
		return searchRequest;
	}

	private QueryBuilder buildQuery(QueryParams params) {

		if (null == params || CommonUtil.isNullOrEmpty(params.getUserInput()) || null == params.getFields()) {
			return QueryBuilders.matchAllQuery();
		}
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		MultiMatchQueryBuilder multiQuery = null;
		if (null != params && !CommonUtil.isNullOrEmpty(params.getUserInput()) && null != params.getFields()) {
			multiQuery = QueryBuilders.multiMatchQuery(params.getUserInput(), getFields(params.getFields()))
					.operator(Operator.AND).type(MultiMatchQueryBuilder.Type.CROSS_FIELDS);
			for (Field field : params.getFields()) {
				multiQuery.field(field.getName(), field.getBoost() == 0 ? 1 : field.getBoost());
			}
			query.must(multiQuery);

			/*			for (String input : params.getUserInput().split("\\s+")) {
							if (!CommonUtil.isNullOrEmpty(input)) {
								for (Field f : params.getFields()) {
									query.should(QueryBuilders.wildcardQuery(f.getName(), String.format("*%s*", input)));
								}
							}
						}
						query.minimumShouldMatch("75%");*/
		}

		if (null != params.getFields()) {
			for (Filter f : params.getFilters()) {
				if (OperateCode.eq == f.getOperate()) {
					query.filter(
							QueryBuilders.termQuery(CommonUtil.objectToString(f.getField().getName()), f.getValue()));
				} else if (OperateCode.lte == f.getOperate()) {
					query.filter(QueryBuilders.rangeQuery(CommonUtil.objectToString(f.getField().getName()))
							.lte(f.getValue()));
				} else if (OperateCode.gte == f.getOperate()) {
					query.filter(QueryBuilders.rangeQuery(CommonUtil.objectToString(f.getField().getName()))
							.gte(f.getValue()));
				}
			}
		}
		return query;
	}

	private String[] getFields(Field[] fields) {
		if (null != fields && fields.length > 0) {
			String[] result = new String[fields.length];
			for (int i = 0; i < fields.length; i++) {
				result[i] = CommonUtil.objectToString(fields[i].getName());
			}
			return result;
		}
		return null;
	}

	private QueryResultSet buildResponse(SearchResponse response) {
		QueryResultSet result = null;
		if (null != response && null != response.getHits()) {
			result = new QueryResultSet();
			result.setMaxScore(response.getHits().getMaxScore());
			result.setTotal(response.getHits().getTotalHits());
			List<Map<String, Object>> datas = new ArrayList<>();
			List<Map<String, Object>> highlightText = new ArrayList<>();
			Map<String, Object> data = null;
			Map<String, Object> highlight = null;
			if (null != response.getHits().getHits()) {
				for (SearchHit responseBody : response.getHits().getHits()) {
					data = new HashMap<>();
					data.putAll(responseBody.getSource());
					data.put(KEY_SCORE, responseBody.getScore());
					datas.add(data);
					if (null != responseBody.getHighlightFields()) {
						highlight = getHighLightText(responseBody.getHighlightFields());
						highlightText.add(highlight);
					}
				}
			}
			result.setDatas(datas);
			result.setHighlightText(highlightText);
			result.setTook(response.getTookInMillis());
		}
		return result;
	}

	private Map<String, Object> getHighLightText(Map<String, HighlightField> highLightFields) {
		if (null != highLightFields) {
			Map<String, Object> highlight = new HashMap<>();
			StringBuilder text = null;
			for (Map.Entry<String, HighlightField> entry : highLightFields.entrySet()) {
				if (null != entry.getValue()) {
					text = new StringBuilder();
					for (Text hltext : entry.getValue().fragments()) {
						text.append(hltext.string());
					}
				}
				highlight.put(entry.getKey(), text.toString());
			}
			return highlight;
		}
		return null;
	}

	@Override
	public QueryResult search(String sql) throws SearchException {
		throw new UnsupportedMethodException("使用NativeClient时，不支持使用这个方法");
	}

	public TransportClient getClient() {
		return client;
	}

	public void setClient(TransportClient client) {
		this.client = client;
	}

}
