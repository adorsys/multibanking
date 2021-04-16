package de.adorsys.multibanking.bg.mapper;

public class BuchungstextMapper {

    /**
     * @param gvcode
     * @return buchungstext or null if nothing matches
     */
    public static String gvcode2Buchungstext(String gvcode) {
        return Gvc2Buchungstext.gvcode2Buchungstext(gvcode);
    }

    /**
     * @param bankTransactionCode
     * @return buchungstext or null if nothing matches
     */
    public static String bankTransactionCode2Buchungstext(String bankTransactionCode) {
        return Btc2Buchungstext.btc2Buchungstext(bankTransactionCode);
    }
}
