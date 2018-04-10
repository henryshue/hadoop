package cn.i.search.core.api;

import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.query.entity.QueryParams;
import cn.i.search.core.api.query.entity.QueryResult;

public interface QueryService {

	public QueryResult search(QueryParams params) throws SearchException;

	public QueryResult search(String sql) throws SearchException;
}
