package cn.i.search.core.elasticsearch.suggest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.i.search.core.api.exception.SearchException;
import cn.i.search.core.api.sugget.SuggestService;
import cn.i.search.core.api.sugget.entity.SuggestParams;
import cn.i.search.core.common.CommonUtil;
import cn.i.search.core.elasticsearch.utils.Constants;

public class NativeSuggestService implements SuggestService {

	private TransportClient client;
	private static final Logger logger = LoggerFactory.getLogger(NativeSuggestService.class);

	public NativeSuggestService(TransportClient client) {
		this.client = client;
	}

	@Override
	public String[] Suggest(String userInput) throws SearchException {
		SuggestParams params = new SuggestParams();
		params.setFields("suggest_content");
		params.setIndex("sms_men");
		params.setLimit(10);
		params.setSuggestQueryName("mysuggest");
		params.setUserInput(userInput);
		List<String> list = completionSuggest(params);

		return list.toArray(new String[] {});
	}

	@Override
	public List<String> completionSuggest(SuggestParams params) throws SearchException {
		Set<String> result = new HashSet<>();
		// 必须参数判断
		if (null != params && !CommonUtil.isNullOrEmpty(params.getFields()) && null != params.getIndex()
				&& params.getIndex().length > 0 && !CommonUtil.isNullOrEmpty(params.getSuggestQueryName())
				&& !CommonUtil.isNullOrEmpty(params.getUserInput())) {
			// 如果没有传条数，给默认值
			if (Constants.SIZE_EMPTY == params.getLimit()) {
				params.setLimit(Constants.SIZE_SUGGEST_DEFAULT);
			}
			SearchRequestBuilder requestBuilder = client.prepareSearch(params.getIndex())
					.setQuery(QueryBuilders.matchAllQuery()).setSize(0)
					.suggest(new SuggestBuilder().addSuggestion(params.getSuggestQueryName(),
							SuggestBuilders.completionSuggestion(params.getFields()).text(params.getUserInput())
									.size(params.getLimit())));
			logger.info("Suggest:" + requestBuilder);
			List<? extends Entry<? extends Option>> resultList = requestBuilder.get().getSuggest()
					.getSuggestion(params.getSuggestQueryName()).getEntries();

			for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> op : resultList) {
				for (Option option : op.getOptions()) {
					if (null != option && null != option.getText()) {
						result.add(option.getText().string());
					}
				}
			}
		}
		return new ArrayList<>(result);
	}

	public List<String> pinyinMatching(SuggestParams params) throws SearchException {
		return null;
	}

	public List<String> RelatedTermsSuggestion(SuggestParams params) throws SearchException {
		return null;
	}

	public TransportClient getClient() {
		return client;
	}

	public void setClient(TransportClient client) {
		this.client = client;
	}

}
