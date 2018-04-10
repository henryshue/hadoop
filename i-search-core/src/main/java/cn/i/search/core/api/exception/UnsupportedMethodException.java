package cn.i.search.core.api.exception;

public class UnsupportedMethodException extends SearchEngineRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4840788910244495594L;

	public UnsupportedMethodException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMethodException(String message) {
		super(message);
	}

}
