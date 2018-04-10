package cn.i.search.core.api.sugget;

import java.util.List;

import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.sugget.entity.SuggestParams;

public interface SuggestService {

	public String[] Suggest(String userInput) throws SearchException;

	public List<String> completionSuggest(SuggestParams params) throws SearchException;

}
