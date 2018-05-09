package de.adorsys.multibanking.utils;

import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

public class FQNUtils {

	public static final DocumentFQN banksFQN(){
		return new DocumentFQN("banks");
	}
	
	public static final DocumentFQN userDataFQN(){
		return new DocumentFQN("mbsUserData.aes");
	}

	private static final DocumentDirectoryFQN userAgentsDirFQN() {
		return new DocumentDirectoryFQN("userAgents");
	}
	private static final DocumentDirectoryFQN userAgentDirFQN(String userAgentId) {
		return userAgentsDirFQN().addDirectory(userAgentId);
	}
	public static final DocumentFQN userAgentCredentialFQN(String userAgentId) {
    	return userAgentDirFQN(userAgentId).addName("credentials.aes");
	}
	
	private static final DocumentDirectoryFQN bankAccessesDirFQN() {
		return new DocumentDirectoryFQN("bankaccesses");
	}

	public static final DocumentDirectoryFQN bankAccessDirFQN(String bankAccessId) {
		return bankAccessesDirFQN().addDirectory(bankAccessId);
	}

	public static final DocumentFQN credentialFQN(String bankAccessId) {
    	return bankAccessDirFQN(bankAccessId).addName("credentials.aes");
	}

	public static final DocumentDirectoryFQN bankAccountsDirFQN(String bankAccessId) {
    	return bankAccessDirFQN(bankAccessId).addDirectory("accounts");
	}

	private static final DocumentDirectoryFQN bankAccountDirFQN(String bankAccessId, String accountId) {
    	return bankAccountsDirFQN(bankAccessId).addDirectory(accountId);
	}

	private static final DocumentDirectoryFQN bankAccountPeriodFQN(String bankAccessId, String accountId, String period) {
    	return bankAccountDirFQN(bankAccessId, accountId).addDirectory(period);
	}
	
	public static DocumentFQN bookingFQN(String accessId, String accountId, String period) {
    	return bankAccountPeriodFQN(accessId, accountId, period).addName("bookings.aes");
	}

//	public static DocumentFQN analyticsFQN(String accessId, String accountId) {
//    	return bankAccountDirFQN(accessId, accountId).addName("analytics.aes");
//	}
	
//	public static DocumentFQN contractsFQN(String accessId, String accountId) {
//    	return bankAccountDirFQN(accessId, accountId).addName("contracts.aes");
//	}
	
//	public static DocumentFQN standingOrdersFQN(String accessId, String accountId) {
//    	return bankAccountDirFQN(accessId, accountId).addName("standingOrders.aes");
//	}

	public static DocumentFQN bookingRulesFQN() {
		return new DocumentFQN("bookingRules.aes");
	}
	public static DocumentFQN bookingCategoriesFQN() {
		return new DocumentFQN("bookingCategories.aes");
	}

	private static final DocumentDirectoryFQN imagesDirFQN() {
		return new DocumentDirectoryFQN("images");
	}

	public static DocumentFQN imageFQN(String imageName) {
		return imagesDirFQN().addName(imageName);
	}

	public static DocumentFQN paymentsFQN(String accessId, String accountId) {
    	return bankAccountDirFQN(accessId, accountId).addName("payments.aes");
	}

	public static DocumentFQN anonymizedBookingFQN(String accessId, String accountId, String period) {
    	return bankAccountPeriodFQN(accessId, accountId, period).addName("anonymizedBookings.aes");
	}

	public static DocumentDirectoryFQN expireDirFQN() {
		return new DocumentDirectoryFQN("users").addDirectory("expiry");
	}
	public static DocumentFQN expireDayFileFQN(String dayDirName) {
		return expireDirFQN().addName(dayDirName);
	}
}
