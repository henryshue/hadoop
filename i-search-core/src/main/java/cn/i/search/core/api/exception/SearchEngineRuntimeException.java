package cn.i.search.core.api.exception;

public class SearchEngineRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5150265112333041900L;

	public SearchEngineRuntimeException() {
		super();
	}

	public SearchEngineRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SearchEngineRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchEngineRuntimeException(String message) {
		super(message);
	}

	public SearchEngineRuntimeException(Throwable cause) {
		super(cause);
	}

}
