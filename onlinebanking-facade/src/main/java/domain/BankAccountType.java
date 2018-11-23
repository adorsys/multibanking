package domain;

/**
 * Created by alexg on 12.09.17.
 */
public enum BankAccountType {

    GIRO, SAVINGS, FIXEDTERMDEPOSIT, DEPOT, LOAN, CREDITCARD, BUIILDINGSAVING, INSURANCE, UNKNOWN;

    private Integer rawType;

    public BankAccountType rawType(Integer rawType) {
        this.rawType = rawType;
        return this;
    }

    public Integer getRawType() {
        return rawType;
    }

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
            return UNKNOWN.rawType(hbciAccountType);

        if (hbciAccountType < 10) {
            return GIRO.rawType(hbciAccountType);
        } else if (hbciAccountType < 20) {
            return SAVINGS.rawType(hbciAccountType);
        } else if (hbciAccountType < 30) {
            return FIXEDTERMDEPOSIT.rawType(hbciAccountType);
        } else if (hbciAccountType < 40) {
            return DEPOT.rawType(hbciAccountType);
        } else if (hbciAccountType < 50) {
            return LOAN.rawType(hbciAccountType);
        } else if (hbciAccountType < 60) {
            return CREDITCARD.rawType(hbciAccountType);
        } else if (hbciAccountType < 70) {
            return DEPOT.rawType(hbciAccountType);
        } else if (hbciAccountType < 80) {
            return BUIILDINGSAVING.rawType(hbciAccountType);
        } else if (hbciAccountType < 90) {
            return INSURANCE.rawType(hbciAccountType);
        }

        return UNKNOWN.rawType(hbciAccountType);
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

    //    CACC("Current"),  // Account used to post debits and credits when no specific account has been nominated
//    CASH("CashPayment"),  // Account used for the payment of cash
//    CHAR("Charges"),  // Account used for charges if different from the account for payment
//    CISH("CashIncome"),  // Account used for payment of income if different from the current cash account
//    COMM("Commission"),  // Account used for commission if different from the account for payment
//    CPAC("ClearingParticipantSettlementAccount"),  // Account used to post settlement debit and credit entries on behalf of a designated Clearing Participant
//    LLSV("LimitedLiquiditySavingsAccount"),  // Account used for savings with special interest and withdrawal terms
//    LOAN("Loan"),  // Account used for loans
//    MGLD("Marginal Lending"),  // Account used for a marginal lending facility
//    MOMA("Money Market"),  // Account used for money markets if different from the cash account
//    NREX("NonResidentExternal"),  // Account used for non-resident external
//    ODFT("Overdraft"),  // Account is used for overdrafts
//    ONDP("OverNightDeposit"),  // Account used for overnight deposits
//    OTHR("OtherAccount"),  // Account not otherwise specified
//    SACC("Settlement"),  // Account used to post debit and credit entries, as a result of transactions cleared and settled through a specific clearing and settlement system
//    SLRY("Salary"),  // Accounts used for salary payments
//    SVGS("Savings"),  // Account used for savings
//    TAXE("Tax"),  // Account used for taxes if different from the account for payment
//    TRAN("TransactingAccount"),  // A transacting account is the most basic type of bank account that you can get. The main difference between transaction and cheque accounts is that you usually do not get a cheque book with your transacting account and neither are you offered an overdraft facility
//    TRAS("Cash Trading");
    public static BankAccountType fromXS2AType(String xs2aAccountType) {
        switch (xs2aAccountType) {
            case "CACC":
            case "CASH":
            case "TRAN":
                return BankAccountType.GIRO;
            case "SVGS":
            case "LLSV":
                return BankAccountType.SAVINGS;
            case "LOAN":
                return BankAccountType.LOAN;
            case "CHAR":
            case "CISH":
            case "COMM":
            case "CPAC":
            case "MGLD":
            case "MOMA":
            case "NREX":
            case "ODFT":
            case "ONDP":
            case "SACC":
            case "SLRY":
            case "TAXE":
            case "OTHR":
                return BankAccountType.UNKNOWN;
        }
        return BankAccountType.UNKNOWN;

    }
}
