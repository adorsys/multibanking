package de.adorsys.multibanking.metrics;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public enum Bankgruppe {
    BUNDESBANK(0, "Bundesbank"),
    SONSTIGE_UND_POSTBANK(1,"Sonstige_Postbank"),
    SONSTIGE_UND_UNICREDIT(2, "Sonstige_Unicredit"),
    SONSTIGE_UND_CONSORSBANK(3, "Sonstige_Consors"),
    COMMERZBANK(4, "Commerzbank"),
    SPARKASSE(5, "Sparkasse"),
    RAIFFEISENBANK(6,"Raiffeisenbank"),
    DEUTSCHE_BANK(7,"Deutsche_Bank"),
    COMMERZBANK_VORMALS_DRESDNER(8,"Commerzbank"),
    VOLKSBANK(9,"Volksbank");

    private int fourthDigit;
    private String tag;

    Bankgruppe(int fourthDigit, String tag) {
        this.fourthDigit = fourthDigit;
        this.tag = tag;
    }

    private static Bankgruppe byBankCode(String bankCode) {
        if (StringUtils.isEmpty(bankCode) || bankCode.length() != 8 || bankCode.matches("\\D+")) {
            return null;
        }
        int fourth = Integer.parseInt(bankCode.substring(3,4));

        return Stream.of(Bankgruppe.values())
            .filter(b -> b.fourthDigit == fourth)
            .findFirst()
            .get();
    }

    static String tagByBankCode(String bankCode) {
        Bankgruppe bankgruppe = byBankCode(bankCode);

        if (bankgruppe != null) {
            return bankgruppe.tag;
        }

        return null;
    }
}
