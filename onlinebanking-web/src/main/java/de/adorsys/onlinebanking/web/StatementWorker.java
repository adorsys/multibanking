package de.adorsys.onlinebanking.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class StatementWorker {
    
    private static final Logger LOG = LoggerFactory.getLogger(StatementWorker.class);
    private static final String LogIdent = "[WebSync:StatementWorker] ";

    public StatementWorker() {
    }

    public static ArrayList sortBankArrayList(ArrayList transactionDataList, boolean reverse) throws Exception {
        
        try {
            if (reverse) {
                Collections.reverse(transactionDataList);
                LOG.trace("[WebSync:StatementWorker] reversed-transactionDataList enthält nun das MatrixArray:\n" + StringUtils.formatedArrayListForLog(transactionDataList));
            }

            ArrayList copyUnsortedDataList = StringUtils.deepCopyMatrixArrayList(transactionDataList);
            LOG.trace("[WebSync:StatementWorker] copyUnsortedDataList enthält nun das MatrixArray:\n" + StringUtils.formatedArrayListForLog(copyUnsortedDataList));
            LOG.trace("[WebSync:StatementWorker] verändertes Original des reversed-transactionDataList enthält nun das MatrixArray:\n" + StringUtils.formatedArrayListForLog(transactionDataList));
            LOG.trace("[WebSync:StatementWorker] sollte gleiche copyUnsortedDataList sein und enthält nun das MatrixArray:\n" + StringUtils.formatedArrayListForLog(copyUnsortedDataList));
            Collections.sort(copyUnsortedDataList, new StringUtils.MatrixArrayListDateComparator());
            LOG.trace("[WebSync:StatementWorker] sollte das nach Datum sortierte copyUnsortedDataList sein und enthält nun das MatrixArray:\n" + StringUtils.formatedArrayListForLog(copyUnsortedDataList));
            ArrayList AllDataDates = new ArrayList();

            for(int i = 0; i < copyUnsortedDataList.size(); ++i) {
                if (i == 0) {
                    AllDataDates.add(((ArrayList)copyUnsortedDataList.get(i)).get(0));
                } else if (((Date)((ArrayList)copyUnsortedDataList.get(i)).get(0)).getTime() != ((Date)((ArrayList)copyUnsortedDataList.get(i - 1)).get(0)).getTime()) {
                    AllDataDates.add(((ArrayList)copyUnsortedDataList.get(i)).get(0));
                }
            }

            LOG.trace("[WebSync:StatementWorker] Liste eindeutiger Beleg-Datum im Auszug:\n" + StringUtils.formatedArrayListForLog(AllDataDates));
            ArrayList DayArray = new ArrayList();
            ArrayList NewFullArray = new ArrayList();

            for(int i = 0; i < AllDataDates.size(); ++i) {
                for(int ArrayNr = 0; ArrayNr < transactionDataList.size(); ++ArrayNr) {
                    if (((Date)((ArrayList)transactionDataList.get(ArrayNr)).get(0)).getTime() == ((Date)AllDataDates.get(i)).getTime()) {
                        LOG.trace("[WebSync:StatementWorker] Transaktion " + ArrayNr + "; " + ((Date)((ArrayList)transactionDataList.get(ArrayNr)).get(0)).getTime() + " = " + ((Date)AllDataDates.get(i)).getTime() + "; Adde Umsatz " + ((ArrayList)transactionDataList.get(ArrayNr)).get(2) + "\tvom " + ((ArrayList)transactionDataList.get(ArrayNr)).get(0) + "\t" + ((ArrayList)transactionDataList.get(ArrayNr)).get(6));
                        DayArray.add(StringUtils.deepCopyObject(transactionDataList.get(ArrayNr)));
                    }
                }

                NewFullArray.addAll(DayArray);
                DayArray.removeAll(DayArray);
            }

            transactionDataList.removeAll(transactionDataList);
            transactionDataList.addAll(NewFullArray);
            LOG.trace("[WebSync:StatementWorker] Liste aller abgeholten Umsätze: (schon fertig formatiert und sortiert, bereit zur Verarbeitung):\n" + StringUtils.formatedArrayListForLog(transactionDataList));
            return transactionDataList;
        } catch (Exception var14) {
            StringWriter sw = new StringWriter();
            var14.printStackTrace(new PrintWriter(sw));
            LOG.error("[WebSync:StatementWorker] Fehler beim Verarbeiten transactionDataList und Umkehr-Copy-Sortier-Funktion\n" + sw.toString());
            throw new Exception("Fehler beim Verarbeiten transactionDataList und Umkehr-Copy-Sortier-Funktion; " + var14.getMessage());
        }
    }

    public static ArrayList createBankArrayListHeader(String InstitutAlias, String AccountNr, double Amount, double avalibleAmount, double unbookedAmount, String fromDateSimple, String toDateSimple) {
        ArrayList BankArrayListHeader = new ArrayList();
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(0)).add("Automatisch generierter Kontoauszug der Java-ScreenScrapingBibliothek 'finance.websync' (beinhaltet die sortierterten, fertig formatierten Umsätze)");
        BankArrayListHeader.add(new ArrayList());
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(2)).add("Institut:");
        ((ArrayList)BankArrayListHeader.get(2)).add(InstitutAlias);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(3)).add("Kontonummer:");
        ((ArrayList)BankArrayListHeader.get(3)).add(AccountNr);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(4)).add("Kontostand:");
        ((ArrayList)BankArrayListHeader.get(4)).add(Amount);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(5)).add("Verfügbarer Betrag:");
        ((ArrayList)BankArrayListHeader.get(5)).add(avalibleAmount);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(6)).add("davon nicht abgerechnet:");
        ((ArrayList)BankArrayListHeader.get(6)).add(unbookedAmount);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(7)).add("Abrufzeitraum:");
        ((ArrayList)BankArrayListHeader.get(7)).add(fromDateSimple + " bis " + toDateSimple);
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(8)).add("Auszugsdatum:");
        ((ArrayList)BankArrayListHeader.get(8)).add((new SimpleDateFormat()).format(Calendar.getInstance().getTime()));
        BankArrayListHeader.add(new ArrayList());
        BankArrayListHeader.add(new ArrayList());
        BankArrayListHeader.add(new ArrayList());
        ((ArrayList)BankArrayListHeader.get(11)).add("Buchungsdatum");
        ((ArrayList)BankArrayListHeader.get(11)).add("Valuta-Datum");
        ((ArrayList)BankArrayListHeader.get(11)).add("Betrag");
        ((ArrayList)BankArrayListHeader.get(11)).add("Saldo");
        ((ArrayList)BankArrayListHeader.get(11)).add("Währung");
        ((ArrayList)BankArrayListHeader.get(11)).add("Umsatzart");
        ((ArrayList)BankArrayListHeader.get(11)).add("Verwendungszweck");
        ((ArrayList)BankArrayListHeader.get(11)).add("IBAN");
        ((ArrayList)BankArrayListHeader.get(11)).add("BIC");
        ((ArrayList)BankArrayListHeader.get(11)).add("Gegenkonto-Name");
        ((ArrayList)BankArrayListHeader.get(11)).add("Gegenkonto-Nummer");
        ((ArrayList)BankArrayListHeader.get(11)).add("Gegenkonto-BLZ");
        ((ArrayList)BankArrayListHeader.get(11)).add("Kommentar");
        ((ArrayList)BankArrayListHeader.get(11)).add("Primanotakennzeichen");
        ((ArrayList)BankArrayListHeader.get(11)).add("Kundenreferenz");
        ((ArrayList)BankArrayListHeader.get(11)).add("Zeilennummer-Auszug");
        return BankArrayListHeader;
    }
}
