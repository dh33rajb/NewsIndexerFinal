package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nikhillo This class represents the smallest indexable unit of text.
 *         At the very least it is backed by a string representation that can be
 *         interchangeably used with the backing char array.
 * 
 *         You are encouraged to add more data structures and fields as you deem
 *         fit.
 */
public class Token {
	// The backing string representation -- can contain extraneous information
	private String termText = null;
	// The char array backing termText
	private char[] termBuffer = null;

	/**
	 * Method to set the termText to given text. This is a sample implementation
	 * and you CAN change this to suit your class definition and data structure
	 * needs.
	 * 
	 * @param text
	 */
	protected void setTermText(String text) {
		if (text != null)
			termText = text;
		else
			termText = "";
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}

	/**
	 * Getter for termText This is a sample implementation and you CAN change
	 * this to suit your class definition and data structure needs.
	 * 
	 * @return the underlying termText
	 */
	public String getTermText() {
		if (termText == null)
			termText = "";
		return termText;
	}

	/**
	 * Method to set the termBuffer to the given buffer. This is a sample
	 * implementation and you CAN change this to suit your class definition and
	 * data structure needs.
	 * 
	 * @param buffer
	 *            : The buffer to be set
	 */
	protected void setTermBuffer(char[] buffer) {
		if (buffer != null) {
			termBuffer = buffer;
			termText = new String(buffer);
		}
	}

	/**
	 * Getter for the field termBuffer
	 * 
	 * @return The termBuffer
	 */
	protected char[] getTermBuffer() {
		return termBuffer;
	}

	/**
	 * Method to merge this token with the given array of Tokens You are free to
	 * update termText and termBuffer as you please based upon your Token
	 * implementation. But the toString() method below must return whitespace
	 * separated value for all tokens combined Also the token order must be
	 * maintained.
	 * 
	 * @param tokens
	 *            The token array to be merged
	 */
	protected void merge(Token... tokens) {
		String text;
		if (this.termText == null)
			text = "";
		else
			text = this.termText;
		String[] PATTERN = { ".", "," };

		if (tokens != null) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] != null && tokens[i].getTermText() != null
						&& (tokens[i].getTermText() != PATTERN[0])
						&& (tokens[i].getTermText() != PATTERN[1])) {
					text = text + " " + tokens[i].termText;
				}
			}
			this.termText = text;
			this.termBuffer = text.toCharArray();
		}
	}

	/**
	 * Returns the string representation of this token. It must adhere to the
	 * following rules: 1. Return only the associated "text" with the token. Any
	 * other information must be suppressed. 2. Must return a non-empty value
	 * only for tokens that are going to be indexed If you introduce special
	 * token types (sentence boundary for example), return an empty string 3. IF
	 * the original token A (with string as "a") was merged with tokens B ("b"),
	 * C ("c") and D ("d"), toString should return "a b c d"
	 * 
	 * @return The raw string representation of the token
	 */
	@Override
	public String toString() {
		// TODO: YOU MUST IMPLEMENT THIS METHOD
		Boolean flag;
		Pattern ptr = Pattern.compile(",");
		String[] PATTERN = { ".", "," };
		String output = null;
		if (this.termText == null) {
			output = "";
			return output;
		} else
			output = this.termText;
		for (int i = 0; i < PATTERN.length; i++) {
			/*
			 * If token text matches any pattern, then null value should be
			 * returned
			 */
			if (this.termText == PATTERN[i])
				output = replacePattern(output, PATTERN[i]);
		}
		return output;
	}

	private String replacePattern(String input, String PATTERN) {
		Pattern ptr = Pattern.compile(PATTERN);
		Matcher matcher = ptr.matcher(input);
		String output = null;
		if (matcher.matches() == true)
			output = matcher.replaceAll("");
		return output;
	}
}
