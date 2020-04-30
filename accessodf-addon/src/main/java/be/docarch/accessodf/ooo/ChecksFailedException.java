package be.docarch.accessodf.ooo;

import java.util.List;

public class ChecksFailedException extends Exception {
	private static final long serialVersionUID = 7337389404514647563L;
	private List<Exception> exceptions;

	public ChecksFailedException(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}
	
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
    	String delim = "";
    	for (Exception exception : exceptions) {
			String message = exception.getMessage();
			sb.append(message);
			sb.append(delim);
			delim = "\n";
		}
    	return sb.toString();
	}
}
