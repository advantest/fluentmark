package net.certiv.fluentmark.core.dot;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

/**
 * Indicates that the parser encountered an unwanted token. Reported by
 * DefaultErrorStrategy#reportMissingToken().
 */
public class MissingTokenException extends RecognitionException {

	private static final long serialVersionUID = 1521061786561517859L;

	public MissingTokenException(Parser recognizer) {
		super(recognizer, recognizer.getInputStream(), recognizer.getContext());
		this.setOffendingToken(recognizer.getCurrentToken());
	}
}
