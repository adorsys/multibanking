package de.adorsys.multibanking.analytics.connector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Provide routines for manipulating creditorIds.
 */
public class DECreditorIdValidator implements CreditorIdValidator {
	private static final String PATTERN = "([deDE]{2}[0-9]{2,2}[A-Za-z0-9]{3,3}[0]{1}[0-9]{10})";
	private static final Pattern regEx = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);
	private static final int length = 18;

    /**
     * Retrieves the first creditor id from the given text.
     * 
     * @param text
     * 
     * @return
     */
	@Override
    public String find(String text) {
        if (text == null || text.length()<18)return null;
        Matcher matcher = regEx.matcher(text);
        if (matcher.find()) return matcher.group(0).toUpperCase();
        return null;
    }
    
    /**
     * Retrieves the invariant part of the creditorId. This is used as a key in the creditorIdMap
     * and normalizes the result string.
     * 
     * For example 
     * 	- trimNormalizedCreditorId(DE70ZZZ00000009476) -> 00000009476
     * 	- trimNormalizedCreditorId(de17aok00000007617) -> 00000007617
     * 
     * @param creditorId
     * @return
     */
	@Override
    public String nationalId(String creditorId){
    	return StringUtils.substring(creditorId, 7).toUpperCase();
    }

	@Override
	public boolean isValid(String creditorId) {
		if(creditorId==null) return false;
		if(creditorId.length()!=18) return false;
		if(StringUtils.startsWithIgnoreCase(creditorId, "de")) return false;
		if("0".equals(creditorId.charAt(7))) return false;
		return creditorId.matches(PATTERN);
	}
}
