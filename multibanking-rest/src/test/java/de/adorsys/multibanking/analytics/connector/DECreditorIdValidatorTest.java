package de.adorsys.multibanking.analytics.connector;

import org.junit.Assert;
import org.junit.Test;

public class DECreditorIdValidatorTest {
	CreditorIdValidator validator = new DECreditorIdValidator();

	@Test
	public void nationalId_shall_return_creditorUniqueId() {
		Assert.assertEquals("00000007617", validator.nationalId("DE17AOK00000007617"));
	}

	@Test
	public void nationalId_lowercase_shall_return_creditorUniqueId_normalized() {
		Assert.assertEquals("00000007617", validator.nationalId("de17aok00000007617"));
	}

    @Test
    public void isValid_shall_return_false_for_String_more_than_18_chars() {
    	Assert.assertFalse(validator.isValid("012345678901234567890123456"));
    }

    @Test
    public void isValid_shall_return_false_for_String_with_no_german_country_code() {
    	Assert.assertFalse(validator.isValid("AT12345678901234567890"));
    }

    @Test
    public void isValid_shall_return_false_for_String_with_german_country_code_and_less_than_18_chars() {
    	Assert.assertFalse(validator.isValid("DE123456789012345"));
    }

    @Test
    public void isValid_shall_return_false_for_String_with_german_country_code_and_more_than_18_chars() {
    	Assert.assertFalse(validator.isValid("DE12ZZZ3456789012345"));
    }

    @Test
    public void isValid_shall_return_false_for_String_with_german_country_code_and_non_zero_at_position_8() {
    	Assert.assertFalse(validator.isValid("DE17AOKO0000007617"));
    }

    @Test
    public void isValid_shall_return_false_for_String_with_german_country_code_and_non_numeric_after_position_8() {
    	Assert.assertFalse(validator.isValid("DE17AOK00000007A17"));
    }

    @Test
    public void find_shall_return_null_for_null_input() {
    	Assert.assertNull(validator.find(null));
    }

    @Test
    public void isValid_shall_return_false_for_String_less_than_18_chars() {
    	Assert.assertFalse(validator.isValid("1234567890"));
    }

    @Test
    public void find_shall_return_value_valid_dreditor_id() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("DE17AOK00000007617"));
    }

    @Test
    public void find_shall_return_normalized_value_valid_dreditor_id() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("de17aok00000007617"));
    }
    
    @Test
    public void find_shall_return_value_in_multiple_strings_seperated_with_whitespaces() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("this string contains a DE17AOK00000007617 as valid creditor id"));
    }

    @Test
    public void find_shall_return_value_in_multiple_strings_seperated_with_providercode_lowercase() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("this string contains a DE17aok00000007617 as valid creditor id"));
    }

    @Test
    public void find_shall_return_value_in_multiple_strings_seperated_with_countrycode_lowercase() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("this string contains a de17AOK00000007617 as valid creditor id"));
    }
    
    @Test
    public void find_shall_return_value_in_multiple_strings_seperated_with_plus() {
    	Assert.assertEquals("DE17AOK00000007617", validator.find("this+string+contains+a+de17AOK00000007617+as+valid+creditor+id"));
    }

    @Test
    public void find_shall_return_null_for_String_more_than_18_chars() {
    	Assert.assertNull(validator.find("contains+a+de012345678901234567890123456+as+invalid+creditor+id"));
    }

    @Test
    public void find_shall_return_null_for_String_with_no_german_country_code() {
    	Assert.assertNull(validator.find("AT12345678901234567890"));
    }

    @Test
    public void find_shall_return_null_for_String_with_german_country_code_and_less_than_18_chars() {
    	Assert.assertNull(validator.find("DE123456789012345"));
    }

    @Test
    public void find_shall_return_null_for_String_with_german_country_code_and_more_than_18_chars() {
    	Assert.assertNull(validator.find("DE12ZZZ3456789012345"));
    }

    @Test
    public void find_shall_return_null_for_String_with_german_country_code_and_non_zero_at_position_8() {
    	Assert.assertNull(validator.find("DE17AOKO0000007617"));
    }

    @Test
    public void find_shall_return_null_for_String_with_german_country_code_and_non_numeric_after_position_8() {
    	Assert.assertNull(validator.find("DE17AOK00000007A17"));
    }
}
