package net.certiv.fluentmark.core.dot;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

/**
 * Indicates that the parser encountered an unwanted token. Reported by
 * DefaultErrorStrategy#reportUnwantedToken().
 */
public class UnwantedTokenException extends RecognitionException {

	private static final long serialVersionUID = 9007702681429153068L;

	public UnwantedTokenException(Parser recognizer) {
		super(recognizer, recognizer.getInputStream(), recognizer.getContext());
		this.setOffendingToken(recognizer.getCurrentToken());
	}
}
