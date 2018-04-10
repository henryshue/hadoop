package cn.i.search.core.api.exception;

public class SearchEngineException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6948525755768277928L;

	public SearchEngineException(String message) {
		super(message);
	}

	public SearchEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchEngineException(Throwable cause) {
		super(cause);
	}

}
