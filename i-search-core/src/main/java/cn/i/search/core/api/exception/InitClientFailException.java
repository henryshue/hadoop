package cn.i.search.core.api.exception;

public class InitClientFailException extends SearchEngineException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6423127558780499405L;

	public InitClientFailException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitClientFailException(String message) {
		super(message);
	}

	public InitClientFailException(Throwable cause) {
		super(cause);
	}

}
