/**
 *
 */

package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.org.tartarus.snowball.ext.porterStemmer;

/**
 * The abstract class that you must extend when implementing your TokenFilter
 * rule implementations. Apart from the inherited Analyzer methods, we would use
 * the inherited constructor (as defined here) to test your code.
 * 
 * @author nikhillo
 * 
 */
public abstract class TokenFilter implements Analyzer {
	protected TokenStream tokenList;
	ArrayList<String> stopList;
	Pattern yearPattern = null;
	Pattern yearPattern1 = null;
	Pattern christYearPattern = null;
	Pattern christYearPattern1 = null;
	Pattern adbcDate = null;
	Pattern timeFormat = null;
	Pattern timeFormat1 = null;
	Pattern numericPattern = null;
	Pattern stemmerPattern = null;
	Pattern finalDateFormat = null;

	private Map<String, String> contractionsMap;

	/**
	 * Default constructor, creates an instance over the given TokenStream
	 * 
	 * @param stream
	 *            : The given TokenStream instance
	 */
	public TokenFilter(TokenStream stream) {
		// TODO : YOU MUST IMPLEMENT THIS METHOD
		tokenList = stream;
		yearPattern = Pattern
				.compile("([\\s]*[12][089][0-9][0-9]([\\-][0-9]?[0-9])?[\\.]?[\\,]?[\\s]*)");
		yearPattern1 = Pattern
				.compile("([\\s]*[12][089][0-9][0-9][\\.]?[\\,]?[\\s]*)");
		christYearPattern = Pattern
				.compile("([\\s]*[0-9]*[\\s]*(BC)?(AD)?[\\.]?[\\,]?[\\s]*)");
		christYearPattern1 = Pattern.compile("([\\s]*(BC)?(AD)?[\\.]?[\\,]?$)");
		adbcDate = Pattern.compile("([\\s]*[0-9]*[\\.]?[\\,]?[\\s]*)");
		timeFormat = Pattern
				.compile("([\\s]*([1]?[0-9]?[:][1]?[0-9]?[\\s]*)*((AM)?(PM)?(am)?(pm)?)[\\.]?[\\,]?[\\s]*)");
		timeFormat1 = Pattern
				.compile("([\\s]*(AM)?(PM)?(am)?(pm)?[\\.]?[\\,]?[\\s]*)");
		numericPattern = Pattern
				.compile("(([\\\"]*[\\s]*[\0]*[\\.]*[0-9]*[\\,]?[\\-]*[\\/]?[\\s]*[0-9]*[\0]*[\\\"]*)*([\\\"]*[\0]*[\\.]*[0-9]*[\\,]?[\\-]*[\\/]?[\\s]*[0-9]*[\0]*[\\s]*[\\\"]*)$)|([\\\"]*[\0]*[\\.]*[0-9]*[\\.]?[\\s]*[0-9]*[\\%]?[\\.]*[\0]*[\\\"]*)");
		stemmerPattern = Pattern.compile("([^a-zA-Z]+[a-zA-Z]*$)");
		finalDateFormat = Pattern
				.compile("([\\s]*[\\-]?[0-9][0-9][0-9][0-9][0-1][0-9][0-2][0-9][\\.]?[\\,]?[\\s]*)");
	}

	public boolean checkDateFormat(String input) {
		String[] tempMonth = { "jan", "feb", "mar", "apr", "may", "jun", "jul",
				"aug", "sept", "oct", "nov", "dec" };
		String[] tempFullMonth = { "january", "february", "march", "april",
				"may", "june", "july", "august", "september", "october",
				"november", "december" };
		Pattern finalDateFormat = Pattern
				.compile("([\\s]*[12][0-9][0-9][0-9][0-1][0-9][0-2][0-9][\\.]?[\\,]?[\\s]*)");
		int index = 0;

		for (String temStr : tempMonth) {
			if (input.toLowerCase().contains(temStr)
					|| input.toLowerCase().contains(tempFullMonth[index]))
				return true;
			index++;
		}

		if (yearPattern.matcher(input).matches())
			return true;
		if (yearPattern1.matcher(input).matches())
			return true;
		if (christYearPattern.matcher(input).matches())
			return true;
		if (christYearPattern1.matcher(input).matches())
			return true;
		if (timeFormat.matcher(input).matches())
			return true;
		if (timeFormat1.matcher(input).matches())
			return true;
		if (finalDateFormat.matcher(input).matches())
			return true;
		else
			return false;
	}

	public TokenStream setNULL(TokenStream stream, int index) {
		stream.removeToken(index);
		return stream;
	}

	public TokenStream process(TokenStream tempTokenStream,
			TokenFilterType tokenType) throws TokenizerException {
		Pattern ptr, ptr1;
		Matcher matcher;
		String PATTERN = null;
		String PATTERN1 = null;
		String input = tempTokenStream.getCurrent().getTermText();
		Token newToken = new Token();
		String day = null;
		int commaFlag = 0;
		int tokenStatus = 0;
		String prevString = null;
		String nextString = null;

		switch (tokenType) {
		case SYMBOL:
			/*
			 * Below code is to remove special characters .,!,? Dheeraj - below
			 * code removes any special character at the end of the sentence, .
			 * ! ? included
			 */
			if (input.indexOf('\'') == 0
					&& input.lastIndexOf('\'') == input.length() - 1) {
				input = input.substring(1, input.length() - 1);
			}
			tokenStatus = tempTokenStream.getStatus();

			for (int i = input.length() - 1; i > 0; i--) {
				char a = input.charAt(i);
				String aString = Character.toString(a);
				String aStringPrev = null;
				if (i > 1) {
					aStringPrev = Character.toString(input.charAt(i - 1));
				}
				if (i > 1 && ((aStringPrev + aString).equals("'s"))) {
					input = input.substring(0, i - 1);
					i--;
				} else if (i > 1 && ((aStringPrev + aString).equals("s'"))) {
					input = input.substring(0, i - 1) + "s";
					break;
				} else if (aString.matches("[a-zA-Z0-9]")) {
					break;
				} else if (aString.equals(".") || aString.equals("!")
						|| aString.equals("?") || aString.equals(",")) {
					input = input.substring(0, i);
				}
			}

			if (ContractionsCollection.populateCOntractionMap().get(input) != null)
				input = ContractionsCollection.populateCOntractionMap().get(
						input);

			PATTERN = "([\\s]*[\\-]+[\\s]*$)";
			ptr = Pattern.compile(PATTERN);
			matcher = ptr.matcher(input);

			if (matcher.matches() == true) {
				if (tokenStatus > 0)
					prevString = tempTokenStream.getToken(tokenStatus - 1)
							.getTermText();
				if (tokenStatus < tempTokenStream.getList().size() - 1) {
					nextString = tempTokenStream.getToken(tokenStatus + 1)
							.getTermText();
				}
				PATTERN = "([\\s]*[A-Za-z]+[\\s]*$)";
				ptr = Pattern.compile(PATTERN);

				if (prevString != null && nextString != null
						&& ptr.matcher(prevString).matches()
						&& ptr.matcher(nextString).matches())
					input = "";
			}

			PATTERN = "([\\s]*[A-Za-z]+[\\s]*[\\-]+[\\s]*[A-Za-z]+[\\s]*$)";
			ptr = Pattern.compile(PATTERN);
			matcher = ptr.matcher(input);

			if (matcher.matches() == true)
				input = input.replace('-', ' ');
			else {
				// PATTERN="(^[\\-].*)|(.*[\\-]$)";
				PATTERN = "(^[\\-]+[^\\-]*)|([^\\-]*[\\-]+$)";
				ptr = Pattern.compile(PATTERN);
				matcher = ptr.matcher(input);
				if (matcher.matches() == true)
					input = input.replaceAll("[\\-]", "");
			}

			/**
			 * Code to check if string is enclosed by single quotes
			 */
			if (tokenStatus > 0)
				prevString = tempTokenStream.getToken(tokenStatus - 1)
						.getTermText();
			if (tokenStatus < tempTokenStream.getList().size() - 1)
				nextString = tempTokenStream.getToken(tokenStatus + 1)
						.getTermText();
			PATTERN = "([\\'][^\\']*$)";
			ptr = Pattern.compile(PATTERN);

			if (input != null && input.length() > 0
					&& ptr.matcher(input).matches() && nextString != null
					&& nextString.length() > 0) {
				PATTERN = "([^\\']*[\\']$)";
				ptr = Pattern.compile(PATTERN);
				if (ptr.matcher(nextString).matches()) {
					input = input.substring(1, input.length());
					nextString = nextString.substring(0,
							nextString.length() - 1);
					newToken.setTermText(nextString);
					tempTokenStream.setValue(newToken,
							tempTokenStream.getStatus() + 1);
				}
			}

			PATTERN = "([^\\']*[\\']$)";
			ptr = Pattern.compile(PATTERN);

			if (input != null && input.length() > 0
					&& ptr.matcher(input).matches() && prevString != null
					&& prevString.length() > 0) {
				PATTERN = "([\\'][^\\']*$)";
				ptr = Pattern.compile(PATTERN);
				if (ptr.matcher(prevString).matches()) {
					input = input.substring(0, input.length() - 1);
					prevString = prevString.substring(1, prevString.length());
					newToken.setTermText(prevString);
					tempTokenStream.setValue(newToken,
							tempTokenStream.getStatus() - 1);
				}
			}

			/**
			 * To decode single quotes as derivative
			 */
			PATTERN = "([A-Za-z][\\']+[\\(][a-z][\\)]$)";
			ptr = Pattern.compile(PATTERN);
			if (input != null && input.length() > 0
					&& ptr.matcher(input).matches())
				input = input.replaceAll("[\\']", "");
			PATTERN = "([d][a-z][\\']+[\\/][d][a-z]$)";
			ptr = Pattern.compile(PATTERN);
			if (input != null && input.length() > 0
					&& ptr.matcher(input).matches())
				input = input.replaceAll("[\\']", "");
			newToken = new Token();
			newToken.setTermText(input);
			tempTokenStream.setValue(newToken, tempTokenStream.getStatus());

			break;
		case SPECIALCHARS:

			PATTERN = "(([\\s]*[^\\%\\$\\@\\~\\+\\<\\>\\'\\/\\,\\\"\\)\\(\\&\\#\\;]*[\\s]*[\\%\\$\\@\\~\\+\\<\\>\\'\\/\\,\\\"\\)\\(\\&\\#\\;]*[\\s]*[^\\%\\$\\@\\~\\+\\<\\>\\'\\/\\,\\\"\\)\\(\\&\\#\\;]*[\\s]*)*)";
			ptr = Pattern.compile(PATTERN);
			matcher = ptr.matcher(input);

			if (matcher.matches() == true) {
				// input.replaceAll("(\\!$)|(\\.$)|(\\?$)|(\\'$)|(\\'s$)", "");
				input = input.replaceAll("\\+", "");
				input = input.replaceAll("<", "");
				input = input.replaceAll(">", "");
				input = input.replaceAll("'", "");
				input = input.replaceAll("/", "");
				input = input.replaceAll(",", "");

				input = input.replaceAll("\"", "");
				input = input.replaceAll("\\)", "");
				input = input.replaceAll("\\(", "");
				input = input.replaceAll("[\\~]", "");
				input = input.replaceAll("&", "");
				input = input.replaceAll("#", "");
				input = input.replaceAll(";", "");
				input = input.replaceAll("@", "");
				input = input.replaceAll("#", "");
				input = input.replaceAll("[\\$]", "");
				input = input.replaceAll("%", "");
				newToken.setTermText(input);
				tempTokenStream.setValue(newToken, tempTokenStream.getStatus());
			}

			PATTERN = "(([\\s]*[^\\*\\^\\=\\:\\;\\|\\_\\\\]*[\\*\\^\\=\\:\\;\\|\\_\\\\]*[^\\*\\^\\=\\:\\;\\|\\_\\\\]*[\\s]*)*)";
			ptr = Pattern.compile(PATTERN);
			matcher = ptr.matcher(input);

			if (matcher.matches()) {
				input = input.replaceAll("[\\*]", "");
				input = input.replaceAll("[\\^]", "");
				input = input.replaceAll("[\\=]", "");
				input = input.replaceAll("[\\:]", "");
				input = input.replaceAll("[\\;]", "");
				input = input.replaceAll("[\\|]", "");
				input = input.replaceAll("[\\_]", "");
				input = input.replaceAll("[\\\\]", "");
				newToken.setTermText(input);
				tempTokenStream.setValue(newToken, tempTokenStream.getStatus());
			}

			PATTERN = "([A-Za-z]+[\\s]*[\\-][\\s]*[A-Za-z]+$)";
			ptr = Pattern.compile(PATTERN);

			if (ptr.matcher(input).matches()) {
				input = input.replaceAll("[\\-]", "");
				newToken.setTermText(input);
				tempTokenStream.setValue(newToken, tempTokenStream.getStatus());
			}

			// PATTERN="([a-z][a-z])";
			break;
		case DATE:

			// PATTERN = "(0-9?0-9):(0-9?0-9):(0-9?0-9?0-90-9)";
			String[] tempMonth = { "jan", "feb", "mar", "apr", "may", "jun",
					"jul", "aug", "sept", "oct", "nov", "dec" };
			String[] tempFullMonth = { "january", "february", "march", "april",
					"may", "june", "july", "august", "september", "october",
					"november", "december" };
			int index = 0;
			tokenStatus = tempTokenStream.getStatus();
			int tokenListLength = tempTokenStream.getList().size();
			String mth = null;

			for (String temStr : tempMonth) {
				if (input.toLowerCase().contains(temStr)
						|| input.toLowerCase().contains(tempFullMonth[index])) {
					String nextNextString = null;
					// String nextString=null;
					String tempTimeStamp = null;
					// String prevString=null;

					if (input.toLowerCase().matches(
							"([\\s]*(" + temStr + ")[\\s]*$)|([\\s]*("
									+ tempFullMonth[index] + ")[\\s]*$)")) {
						if (tempTokenStream.getStatus() >= 0
								&& tokenStatus < tokenListLength - 1) {
							/* Field for day of the month */
							nextString = tempTokenStream.getToken(
									tokenStatus + 1).getTermText();

							if (tokenStatus < tokenListLength - 2) {
								/* Field for year */
								nextNextString = tempTokenStream.getToken(
										tokenStatus + 2).getTermText();
								if (tokenStatus < tokenListLength - 3)
									/* Field for timestamp */
									tempTimeStamp = tempTokenStream.getToken(
											tokenStatus + 3).getTermText();
							}
							if (tokenStatus > 1)
								prevString = tempTokenStream.getToken(
										tokenStatus - 1).getTermText();
						} else if (tokenStatus > 0
								&& tokenStatus == tokenListLength - 1) {
							prevString = tempTokenStream.getToken(
									tokenStatus - 1).getTermText();
						}
					} else {
						int monthIndex = 0;
						String begString = null;
						String trailString = null;
						if (input.toLowerCase().contains(temStr)) {
							monthIndex = input.toLowerCase().indexOf(temStr);
							if ((monthIndex + temStr.length()) < input.length() - 1) {
								trailString = input.substring(monthIndex + 1);
								nextString = trailString;
							}

						} else if (input.toLowerCase().contains(
								tempFullMonth[index])) {
							monthIndex = input.indexOf(tempFullMonth[index]);
							if ((monthIndex + tempFullMonth[index].length()) < input
									.length() - 1) {
								trailString = input.substring(monthIndex + 1);
								nextString = trailString;
							}

						}

						if (monthIndex > 0) {
							begString = input.substring(0, monthIndex - 1);
							prevString = begString;
						}

					}

					/* Pattern for day of the month detection */
					ptr = Pattern.compile("([\\s]*[1-3]?[0-9]?[\\,]?[\\s]*$)");
					ptr1 = Pattern
							.compile("([\\s]*[1-2][0-9][0-9][0-9]?[\\,]?[\\s]*$)");
					if (nextString != null
							&& ptr.matcher(nextString).matches() == true) {
						/*
						 * If all year,month and day are present , set date in
						 * YYYYMMDD format
						 */
						if (nextNextString != null
								&& ptr1.matcher(nextNextString).matches() == true) {
							/*
							 * Checking if timestamp is present, else set
							 * timestamp as 00:00:00
							 */
							ptr = Pattern
									.compile("([\\s]*[0-2]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*$)");
							if (tempTimeStamp != null
									&& ptr.matcher(tempTimeStamp).matches() == true) {
								tempTimeStamp = nextNextString + index
										+ nextString + " " + tempTimeStamp;
								Token tempToken = new Token();
								tempToken.setTermText(tempTimeStamp);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 3);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus());
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() + 2);
								break;
							} else {
								tempTimeStamp = "00:00:00";
								mth = Integer.toString((index + 1));
								mth = ((index + 1) < 10) ? "0" + mth : mth;
								nextString = nextString.replaceAll("\\,", "");
								if (nextNextString.contains(",")) {
									nextNextString = nextNextString.replaceAll(
											"\\,", "");
									commaFlag = 1;
								}

								nextString = (nextString.length() < 2) ? "0"
										+ nextString : nextString;

								if (commaFlag == 0)
									nextNextString = nextNextString + mth
											+ nextString;
								else {
									nextNextString = nextNextString + mth
											+ nextString + ",";
									commaFlag = 0;
								}
								Token tempToken = new Token();
								tempToken.setTermText(nextNextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 2);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus());
								break;
							}

							// break;
						}
						/* if year field is not present */
						else if (nextNextString != null
								&& ptr1.matcher(nextNextString).matches() == false) {
							// input="1900"+index+nextString+" "+"00:00:00";
							mth = Integer.toString((index + 1));
							mth = ((index + 1) < 10) ? "0" + mth : mth;
							nextString = (nextString.length() < 2) ? "0"
									+ nextString : nextString;
							input = "1900" + mth + nextString;
							Token tempToken = new Token();
							tempToken.setTermText(input);
							tempTokenStream.setValue(tempToken,
									tempTokenStream.getStatus() + 1);
							tempTokenStream = this.setNULL(tempTokenStream,
									tempTokenStream.getStatus());

							break;
						}

					}
					if (prevString != null) {
						ptr = Pattern.compile("([\\s]*[1-3][0-9]?[\\s]*$)");
						/* ptr1 = Pattern.compile("([\\s]*[0-9]{1,4}[\\s]*$)"); */
						/* Year pattern */
						ptr1 = Pattern
								.compile("([\\s]*[1-2][0-9][0-9][0-9]?[\\,]?[\\s]*$)");
						/* to check if day and year field are present */
						if (nextString != null
								&& ptr.matcher(prevString).matches() == true
								&& ptr1.matcher(nextString).matches() == true) {
							// nextString=nextString+index+prevString+"00:00:00";

							ptr = Pattern
									.compile("([\\s]*[0-2]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*$)");
							if (tempTimeStamp != null
									&& ptr.matcher(nextNextString).matches() == true) {
								tempTimeStamp = nextString + index + prevString
										+ nextNextString;
								Token tempToken = new Token();
								tempToken.setTermText(input + " "
										+ tempTimeStamp);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 2);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus());
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								break;
							} else {
								mth = Integer.toString((index + 1));
								mth = ((index + 1) < 10) ? "0" + mth : mth;
								prevString = (prevString.length() < 2) ? "0"
										+ prevString : prevString;
								nextString = nextString + mth + prevString;
								Token tempToken = new Token();
								tempToken.setTermText(nextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								tempTokenStream.resetFlag();
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								tempTokenStream.resetFlag();
								break;
							}

						}/* Year field is not present, So date is day-month */
						else if (nextString != null
								&& ptr.matcher(prevString).matches() == true
								&& ptr1.matcher(nextString).matches() == false) {

							ptr = Pattern
									.compile("([\\s]*[0-2]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*$)");
							if (nextString != null
									&& ptr.matcher(nextString).matches() == true) {
								nextString = "1900" + index + prevString
										+ nextString;
								Token tempToken = new Token();
								tempToken.setTermText(nextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus());
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								break;
							} else {
								tempTimeStamp = "00:00:00";
								mth = Integer.toString((index + 1));
								mth = ((index + 1) < 10) ? "0" + mth : mth;
								prevString = (prevString.length() < 2) ? "0"
										+ prevString : prevString;
								nextString = "1900" + mth + prevString;
								Token tempToken = new Token();
								tempToken.setTermText(nextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus());

								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								break;
							}

						} else if (prevString != null
								&& ptr1.matcher(prevString).matches() == true) {
							/* If year is before month */
							ptr = Pattern
									.compile("([\\s]*[0-2]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*[\\:][\\s]*[0-1]?[0-9][\\s]*$)");
							if (nextString != null
									&& ptr.matcher(nextString).matches() == true) {
								nextString = prevString + index + "01"
										+ nextString;
								Token tempToken = new Token();
								tempToken.setTermText(nextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus() + 1);
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus());
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								break;
							} else {
								tempTimeStamp = "00:00:00";
								// nextString= prevString +
								// index+"01"+" "+"00:00:00";
								mth = Integer.toString((index + 1));
								mth = ((index + 1) < 10) ? "0" + mth : mth;
								// prevString =
								// (prevString.length()<2)?"0"+prevString:prevString;
								day = "01";
								nextString = prevString + mth + day;
								Token tempToken = new Token();
								tempToken.setTermText(nextString);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus());

								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
								break;
							}

						}/*
						 * if previous string is date and current string is
						 * month
						 */
						else if (nextString == null && prevString != null
								&& ptr.matcher(prevString).matches() == true) {
							mth = Integer.toString((index + 1));
							mth = ((index + 1) < 10) ? "0" + mth : mth;
							prevString = (prevString.length() < 2) ? "0"
									+ prevString : prevString;
							day = prevString;
							input = "1900" + mth + day;
							Token tempToken = new Token();
							tempToken.setTermText(input);
							tempTokenStream.setValue(tempToken,
									tempTokenStream.getStatus());
							tempTokenStream = this.setNULL(tempTokenStream,
									tempTokenStream.getStatus() - 1);
							break;
						}
					}
					/*
					 * If year,day and timestamp, all are not present, set day
					 * as 1st, year as 1900 and timestamp as 00:00:00
					 */
					if (prevString == null && nextString != null
							&& ptr1.matcher(nextString).matches() == true) {
						// input="1900"+index+"01"+" "+"00:00:00";
						mth = Integer.toString((index + 1));
						mth = ((index + 1) < 10) ? "0" + mth : mth;
						day = "01";
						input = nextString + mth + day;
						Token tempToken = new Token();
						tempToken.setTermText(input);
						tempTokenStream.setValue(tempToken,
								tempTokenStream.getStatus() + 1);
						/*
						 * tempTokenStream = this.setNULL(tempTokenStream,
						 * tempTokenStream.getStatus() - 1);
						 */
						tempTokenStream = this.setNULL(tempTokenStream,
								tempTokenStream.getStatus());
						tempTokenStream.resetFlag();
						/*
						 * tempTokenStream = this.setNULL(tempTokenStream,
						 * tempTokenStream.getStatus());
						 * tempTokenStream.resetFlag();
						 */
						break;
					}/*
					 * if only day is present after month and year is not
					 * present
					 */
					if (prevString == null && nextString != null
							&& ptr.matcher(nextString).matches() == true) {
						mth = Integer.toString((index + 1));
						mth = ((index + 1) < 10) ? "0" + mth : mth;
						day = nextString;
						day = (day.length() < 2) ? "0" + day : day;
						input = "1900" + mth + day;
						if (nextString.contains(".")) {
							input = input + ".";
						} else if (nextString.contains(",")) {
							input = input + ",";
						}
						Token tempToken = new Token();
						tempToken.setTermText(input);
						tempTokenStream.setValue(tempToken,
								tempTokenStream.getStatus() + 1);
						tempTokenStream = this.setNULL(tempTokenStream,
								tempTokenStream.getStatus());
						tempTokenStream.resetFlag();
						break;
					} /*
					 * else { mth = Integer.toString((index + 1)); mth = ((index
					 * + 1) < 10) ? "0" + mth : mth; day = "01"; input = "1900"
					 * + mth + day;
					 * 
					 * Token tempToken = new Token();
					 * tempToken.setTermText(input);
					 * tempTokenStream.setValue(tempToken,
					 * tempTokenStream.getStatus()); break; }
					 */
				}
				/* Logic to check if text is just a year */
				else if (yearPattern.matcher(input).matches()) {

					if (tokenStatus > 0
							&& tokenStatus < tempTokenStream.getList().size() - 1
							&& checkDateFormat(tempTokenStream.getToken(
									tokenStatus + 1).getTermText()))
						break;

					if (yearPattern1.matcher(input).matches()) {
						mth = "01";
						day = "01";
						input = input + mth + day;
						Token tempToken = new Token();
						tempToken.setTermText(input);
						tempTokenStream.setValue(tempToken,
								tempTokenStream.getStatus());
						break;
					} else {
						if (input.contains(".")) {
							commaFlag = 1;
							input = input.replaceAll("[\\.]", "");
						} else if (input.contains(",")) {
							commaFlag = 2;
							input = input.replaceAll("[\\,]", "");
						}
						String str[] = input.split("[\\-]");
						int thisYear = Integer.parseInt(str[0].substring(
								str[0].length() - 3, str[0].length()));
						int NextYear = Integer.parseInt(str[1]);
						NextYear = Integer.parseInt(str[0]) + NextYear
								- thisYear;
						mth = "01";
						day = "01";
						input = str[0] + mth + day + "-"
								+ Integer.toString(NextYear) + mth + day;
						if (commaFlag == 1)
							input = input + ".";
						else if (commaFlag == 2)
							input = input + ",";
						Token tempToken = new Token();
						tempToken.setTermText(input);
						tempTokenStream.setValue(tempToken,
								tempTokenStream.getStatus());
					}
				}/* Logic to check if text is year in format of AD and BC */
				else if (christYearPattern.matcher(input).matches()) {
					/**
					 * If there is space between year and AD/BC
					 */
					if (christYearPattern1.matcher(input).matches()) {
						prevString = tempTokenStream.getToken(tokenStatus - 1)
								.getTermText();
						if (adbcDate.matcher(prevString).matches()) {
							prevString.replaceAll("[\\s]", "");
							for (int i = 0; i <= (4 - prevString.length()); i++)
								prevString = "0" + prevString;
							prevString = prevString + "01" + "01";
							if (input.contains(","))
								prevString = prevString + ",";
							else if (input.contains("."))
								prevString = prevString + ".";
							if (input.contains("BC"))
								input = "-" + prevString;
							Token tempToken = new Token();
							tempToken.setTermText(input);
							tempTokenStream.setValue(tempToken,
									tempTokenStream.getStatus());
							tempTokenStream = this.setNULL(tempTokenStream,
									tempTokenStream.getStatus() - 1);
							break;
						}
					} else {
						String str[] = null;
						String year = null;
						String adbcString = null;

						if (input.contains("AD")) {
							str = input.split("(AD)");
							adbcString = "AD";
						} else if (input.contains("BC")) {
							str = input.split("(BC)");
							adbcString = "BC";
						} else if (input.contains("ad")) {
							str = input.split("(ad)");
							adbcString = "ad";
						} else if (input.contains("bc")) {
							str = input.split("(bc)");
							adbcString = "bc";
						}

						if (str != null && str.length > 0) {
							year = str[0];

							for (int i = 0; i < 4 - str[0].length(); i++)
								year = "0" + year;
							year = year + "01" + "01";
							if (input.contains(","))
								year = year + ",";
							else if (input.contains("."))
								year = year + ".";
							if (input.contains("BC"))
								input = "-" + year;
							else
								input = year;
							Token tempToken = new Token();
							tempToken.setTermText(input);
							tempTokenStream.setValue(tempToken,
									tempTokenStream.getStatus());
						}
					}
					break;
				} else if ((input.contains("AM") || input.contains("PM")
						|| input.contains("am") || input.contains("pm"))
						&& timeFormat.matcher(input).matches()) {
					String tempTime[];

					if (!timeFormat1.matcher(input).matches()) {
						String str[] = null;
						if (input.contains("AM"))
							str = input.split("(AM)");
						else if (input.contains("PM"))
							str = input.split("(PM)");
						else if (input.contains("am"))
							str = input.split("(am)");
						else if (input.contains("pm"))
							str = input.split("(pm)");

						input.replaceAll("[\\s]", "");
						if (str.length > 0) {
							tempTime = str[0].split("[\\:]");
							if (tempTime.length > 0) {
								tempTime[0] = tempTime[0].length() < 2 ? "0"
										+ tempTime[0] : tempTime[0];
								tempTime[1] = tempTime[1].length() < 2 ? "0"
										+ tempTime[1] : tempTime[1];
								if (input.contains("PM")
										|| input.contains("pm")) {
									int railTime = Integer
											.parseInt(tempTime[0]) + 12;

									tempTime[0] = Integer.toString(railTime);// tempTime[0]+12;
								}
								if (input.contains("."))
									input = tempTime[0] + ":" + tempTime[1]
											+ ":" + "00.";
								if (input.contains(","))
									input = tempTime[0] + ":" + tempTime[1]
											+ ":" + "00,";
								Token tempToken = new Token();
								tempToken.setTermText(input);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus());
							}
						}
					} else {
						// ptr =
						// Pattern.compile("([\\s]*[0-2]?[1-9]?[\\,]?[\\.]?[\\s]*$)");
						if (tokenStatus > 0)
							prevString = tempTokenStream.getToken(
									tokenStatus - 1).getTermText();
						if (prevString != null && prevString.length() > 0
								&& prevString.contains(":")) {
							tempTime = prevString.split("[\\:]");

							if (tempTime != null && tempTime.length > 1) {
								tempTime[0] = tempTime[0].length() < 2 ? "0"
										+ tempTime[0] : tempTime[0];
								tempTime[1] = tempTime[1].length() < 2 ? "0"
										+ tempTime[1] : tempTime[1];
								if (input.contains("PM")
										|| input.contains("pm"))
									tempTime[0] = tempTime[0] + 12;
								// input.replaceAll("(PM)?(pm)?(am)?(AM)?");
								tempTime[1].replaceAll("[\\,]?[\\.]?", "");
								tempTime[0] = tempTime[0].length() < 2 ? "0"
										+ tempTime[0] : tempTime[0];
								tempTime[1] = tempTime[1].length() < 2 ? "0"
										+ tempTime[1] : tempTime[1];
								if (input.contains("."))
									input = tempTime[0] + ":" + tempTime[1]
											+ ":" + "00.";
								else if (input.contains(","))
									input = tempTime[0] + ":" + tempTime[1]
											+ ":" + "00,";
								else
									input = tempTime[0] + ":" + tempTime[1]
											+ ":" + "00";
								Token tempToken = new Token();
								tempToken.setTermText(input);
								tempTokenStream.setValue(tempToken,
										tempTokenStream.getStatus());
								tempTokenStream = this.setNULL(tempTokenStream,
										tempTokenStream.getStatus() - 1);
							}
						}

					}
				}

				index++;
			}
			break;

		case NUMERIC:
			matcher = numericPattern.matcher(input);
			if (matcher.matches() == true) {
				PATTERN = "[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][\\s][0-9][0-9][\\:][0-9][0-9][\\:][0-9][0-9]";
				ptr = Pattern.compile(PATTERN);
				matcher = ptr.matcher(input);
				tokenStatus = tempTokenStream.getStatus();
				/**
				 * To check if previous or next string is in date format
				 */
				if (tokenStatus > 0)
					prevString = tempTokenStream.getToken(tokenStatus - 1)
							.getTermText();
				if (tokenStatus < tempTokenStream.getList().size() - 1)
					nextString = tempTokenStream.getToken(tokenStatus + 1)
							.getTermText();

				if (prevString != null && prevString.length() > 0
						&& checkDateFormat(prevString))
					break;
				if (nextString != null && nextString.length() > 0
						&& checkDateFormat(nextString))
					break;
				if (input != null && input.length() > 0
						&& finalDateFormat.matcher(input).matches())
					break;

				if (matcher.matches() == false) {
					if (input.contains("/"))
						input = "/";
					else if (input.contains("%"))
						input = "%";
					else {
						input = "";
						/* setNull function sets text as NULL for the token */
						tempTokenStream = this.setNULL(tempTokenStream,
								tempTokenStream.getStatus());
						break;
					}
					newToken.setTermText(input);
					tempTokenStream.setValue(newToken,
							tempTokenStream.getStatus());
				}
			}

			/*
			 * newToken.setTermText(input);
			 * tempTokenStream.setValue(newToken,tempTokenStream.getStatus());
			 */

			break;

		case CAPITALIZATION:
			PATTERN = "[A-Z]*";
			ptr = Pattern.compile(PATTERN);
			tokenStatus = tempTokenStream.getStatus();
			matcher = ptr.matcher(input);
			int streamSize = tempTokenStream.getList().size();

			PATTERN1 = "((([A-Z][a-z]*)*([\\s])*)*([A-Z][a-z]*)$)";
			if (Pattern.compile(PATTERN1).matcher(input).matches() == true) {
				if (tempTokenStream.getStatus() > 0)
					prevString = tempTokenStream.getToken(tokenStatus - 1)
							.getTermText();
				if (tempTokenStream.getStatus() < streamSize - 1)
					nextString = tempTokenStream.getToken(tokenStatus + 1)
							.getTermText();
				PATTERN = "(([A-Z][a-z]*)*([\\s])*([A-Z][a-z]*)$)";
				ptr = Pattern.compile(PATTERN);
				matcher = ptr.matcher(input);
				PATTERN1 = "(.*[\\.]$)";
				/* Condition to check if taken is first word of a sentence */
				if (prevString == null
						|| Pattern.compile(PATTERN1).matcher(prevString)
								.matches() == true) {
					if (nextString != null
							&& ptr.matcher(nextString).matches() == false) {
						input = input.toLowerCase();
						newToken.setTermText(input);
						tempTokenStream.setValue(newToken,
								tempTokenStream.getStatus());
					} else if (nextString != null) {
						Token tempToken = new Token();
						tempToken.setTermText(input + " " + nextString);
						tempTokenStream.setValue(tempToken,
								tempTokenStream.getStatus() + 1);

						/* setNull function sets text as NULL for the token */
						tempTokenStream = this.setNULL(tempTokenStream,
								tempTokenStream.getStatus());
					}
				} else if (prevString != null
						&& ptr.matcher(prevString).matches() == true) {
					Token tempToken = new Token();
					tempToken.setTermText(prevString + " " + input);
					tempTokenStream.setValue(tempToken,
							tempTokenStream.getStatus());
					/* setNull function sets text as NULL for the token */
					tempTokenStream = this.setNULL(tempTokenStream,
							tempTokenStream.getStatus() - 1);
				} else if (nextString != null
						&& ptr.matcher(nextString).matches() == true) {
					Token tempToken = new Token();
					tempToken.setTermText(input + " " + nextString);
					tempTokenStream.setValue(tempToken,
							tempTokenStream.getStatus() + 1);
					/* setNull function sets text as NULL for the token */
					tempTokenStream = this.setNULL(tempTokenStream,
							tempTokenStream.getStatus());
				}
			}
			break;

		case STEMMER:

			if (!stemmerPattern.matcher(input).matches()) {
				porterStemmer stemmer = new porterStemmer();
				// set the word to be stemmed
				stemmer.setCurrent(input);
				if (stemmer.stem())
					// If stemming is successful obtain the stem of the given
					// word
					input = stemmer.getCurrent();
			}
			newToken.setTermText(input);
			tempTokenStream.setValue(newToken, tempTokenStream.getStatus());
			break;

		case STOPWORD:
			for (String tempStr : StopWordsList.getStopList()) {
				if (input.trim().toLowerCase().equals(tempStr)) {
					tempTokenStream = this.setNULL(tempTokenStream,
							tempTokenStream.getStatus());
					break;
				}
			}
			break;

		case ACCENT:
			AccentRemoval accString = new AccentRemoval(tokenList.getToken());
			Token tempToken = accString.remove();
			tempTokenStream.setValue(tempToken, tempTokenStream.getStatus());
		}
		return tempTokenStream;
	}
}