package cn.i.search.core.api.exception;

public class SearchException extends SearchEngineException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5674990997507349801L;

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchException(String message) {
		super(message);
	}

	public SearchException(Throwable cause) {
		super(cause);
	}

}
