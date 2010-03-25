/**
 */
package at.jku.semwiq.ctrl;

/**
 * @author dorgon
 *
 */
public class SemWIQControllerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7762248237021938518L;

	/**
	 * 
	 */
	public SemWIQControllerException() {
	}

	/**
	 * @param message
	 */
	public SemWIQControllerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SemWIQControllerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SemWIQControllerException(String message, Throwable cause) {
		super(message, cause);
	}

}
