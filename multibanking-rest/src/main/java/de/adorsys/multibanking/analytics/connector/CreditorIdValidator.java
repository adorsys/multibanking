package de.adorsys.multibanking.analytics.connector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Validator interface for creditor ids.
 */
public interface CreditorIdValidator {

    /**
     * Retrieves the first creditor id from the given text.
     * 
     * @param text
     * 
     * @return
     */
    public String find(String text);
    
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
    public String nationalId(String creditorId);
    
    /**
     * Checks if this String is a valid creditor id.
     * 
     * @param creditorId
     * @return
     */
    public boolean isValid(String creditorId);
}
