package domain;

/**
 * Created by alexg on 12.09.17.
 */
public enum BankAccountType {

    GIRO, SAVINGS, FIXEDTERMDEPOSIT, DEPOT, LOAN, CREDITCARD, BUIILDINGSAVING, INSURANCE, UNKNOWN;

    //Klassifizierung der Konten. Innerhalb der vorgegebenen Codebereiche sind kreditinstitutsindividuell bei Bedarf weitere Kontoarten möglich.
    //Codierung:
    //1 – 9: Kontokorrent-/Girokonto
    //10 – 19: Sparkonto
    //20 – 29: Festgeldkonto (Termineinlagen)
    //30 – 39: Wertpapierdepot
    //40 – 49: Kredit-/Darlehenskonto
    //50 – 59: Kreditkartenkonto
    //60 – 69: Fonds-Depot bei einer Kapitalanlagegesellschaft
    //70 – 79: Bausparvertrag
    //80 – 89: Versicherungsvertrag
    //90 – 99: Sonstige (nicht zuordenbar)
    public static BankAccountType fromHbciType(Integer hbciAccountType) {
        if (hbciAccountType == null || hbciAccountType == 0)
            return UNKNOWN;

        if (hbciAccountType < 10) {
            return GIRO;
        } else if (hbciAccountType < 20) {
            return SAVINGS;
        } else if (hbciAccountType < 30) {
            return FIXEDTERMDEPOSIT;
        } else if (hbciAccountType < 40) {
            return DEPOT;
        } else if (hbciAccountType < 50) {
            return LOAN;
        } else if (hbciAccountType < 60) {
            return CREDITCARD;
        } else if (hbciAccountType < 70) {
            return DEPOT;
        } else if (hbciAccountType < 80) {
            return BUIILDINGSAVING;
        } else if (hbciAccountType < 90) {
            return INSURANCE;
        } else if (hbciAccountType < 100) {
            return UNKNOWN;
        }

        return UNKNOWN;
    }

    public static BankAccountType fromFigoType(String type) {
        if (type == null) {
            return UNKNOWN;
        }

        if (type.equalsIgnoreCase("Giro account")) {
            return GIRO;
        }
        if (type.equalsIgnoreCase("Credit card")) {
            return CREDITCARD;
        }
        if (type.equalsIgnoreCase("Savings account")) {
            return SAVINGS;
        }
        if (type.equalsIgnoreCase("Depot")) {
            return DEPOT;
        }
        if (type.equalsIgnoreCase("Loan account")) {
            return LOAN;
        }

        return UNKNOWN;
    }

    //1 = Checking,
    //2 = Savings,
    //3 = CreditCard,
    //4 = Security,
    //5 = Loan,
    //6 = Pocket (DEPRECATED; will not be returned for any account unless this type has explicitly been set via PATCH),
    //7 = Membership
    public static BankAccountType fromFinapiType(Integer type) {
        if (type == null) {
            return UNKNOWN;
        }

        switch (type) {
            case 1:
                return GIRO;
            case 2:
                return SAVINGS;
            case 3:
                return CREDITCARD;
            case 4:
                return INSURANCE;
            case 5:
                return LOAN;
            case 7:
                return DEPOT;
        }

        return UNKNOWN;
    }
}
