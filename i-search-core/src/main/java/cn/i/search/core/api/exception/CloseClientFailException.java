package cn.i.search.core.api.exception;

public class CloseClientFailException extends SearchEngineException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6291621798726298410L;

	public CloseClientFailException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloseClientFailException(String message) {
		super(message);
	}

	public CloseClientFailException(Throwable cause) {
		super(cause);
	}

}
