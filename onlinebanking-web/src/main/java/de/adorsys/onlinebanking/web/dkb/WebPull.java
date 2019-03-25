package de.adorsys.onlinebanking.web.dkb;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import de.adorsys.onlinebanking.web.StatementWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebPull {

    private static final Logger LOG = LoggerFactory.getLogger(WebPull.class);

    public static ArrayList getTransData(WebClient syncWebClient, String Kontonummer, String creditCardNumber,
                                         String fromDateSimple, String toDateSimple, HtmlPage activePage) throws Exception {

        validateCreditCardNumber(creditCardNumber);
        HtmlPage creditCardPage = goToCreditCardPage(syncWebClient, activePage);

        selectCreditCard(creditCardPage, creditCardNumber);
        creditCardPage = selectTransactionStatus(creditCardPage);
        selectDateRange(fromDateSimple, toDateSimple, creditCardPage);
        submitSearch(creditCardPage);
        String csvResponse = goToCsvPage(syncWebClient);

        return parseCSV(Kontonummer, fromDateSimple, toDateSimple, csvResponse);
    }

    private static ArrayList parseCSV(String kontonummer, String fromDateSimple, String toDateSimple,
                                      String csvResponse) throws Exception {
        LOG.trace("csvResponse: \n\n====================== BEGIN DATA ======================\n" + csvResponse + "\n" +
                "======================= END DATA =======================\n");
        if (!csvResponse.contains("<html") && !csvResponse.contains("<head")) {
            String[][] csvdatarow = de.adorsys.onlinebanking.web.StringUtils.data2matrixArray(csvResponse, ';');
            double amount = 0.0D / 0.0;
            boolean foundAmount = false;

            for (int i = 0; i < csvdatarow.length; ++i) {
                if (csvdatarow[i].length > 0 && csvdatarow[i][0].equals("Saldo:")) {
                    LOG.debug("Saldo des RAW-Kontoauszuges (csvdatarow): " + csvdatarow[i][1]);
                    if (StringUtils.isBlank(csvdatarow[i][1])) {
                        throw new Exception("Guthaben von der Deutsche Kreditbank AG (DKB) nicht angegeben! Fehlt im " +
                                "Kontoauszug");
                    }

                    amount =
                            Double.parseDouble(de.adorsys.onlinebanking.web.StringUtils.shrinkString(csvdatarow[i][1]).replaceAll("&nbsp;", "").replaceAll(" ", "").replaceAll("€", "").replaceAll("EUR", "").replaceAll("&euro;", "").replaceAll("\\+", "").replaceAll("\\s", "").trim());
                    LOG.debug("amount: " + amount);
                    foundAmount = true;
                    break;
                }
            }

            if (!foundAmount) {
                throw new Exception("Guthaben von der Deutsche Kreditbank AG (DKB) nicht gefunden! (Bitte dem " +
                        "Entwickler im Forum melden)");
            } else {
                String[] dataResponse = new String[]{csvResponse, Double.toString(amount)};
                String[][] matrixArrayData = csvdatarow;
                ArrayList transactionDataList = new ArrayList();
                int arrayRowCount = 0;

                for (int i = 0; i < matrixArrayData.length; ++i) {
                    if (matrixArrayData[i] != null && !StringUtils.isBlank(matrixArrayData[i][0])) {
                        LOG.trace("matrixArrayData: Zeile Nr. " + i + " wurde beachtet da diese nicht leer ist ... " +
                                "und wird nun auf eine Umsatzzeile gepürft ...");
                        if ((csvdatarow[i][0].equals("Ja") || csvdatarow[i][0].equals("Nein")) && !StringUtils.isBlank(csvdatarow[i][1]) && !StringUtils.isBlank(csvdatarow[i][2])) {
                            try {
                                transactionDataList.add(new ArrayList());
                                String[] arraydatum = matrixArrayData[i][2].substring(0, 10).split("\\.");
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new Date(Integer.parseInt(arraydatum[2], 10) - 1900, Integer.parseInt(arraydatum[1], 10) - 1, Integer.parseInt(arraydatum[0], 10)));
                                LOG.trace("transactionDataList[" + arrayRowCount + "][0]  hat nun folgenden Inhalt: " +
                                        "(Datum)               : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(0));
                                String[] arrayvaluta = matrixArrayData[i][1].substring(0, 10).split("\\.");
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new Date(Integer.parseInt(arrayvaluta[2], 10) - 1900, Integer.parseInt(arrayvaluta[1], 10) - 1, Integer.parseInt(arrayvaluta[0], 10)));
                                LOG.trace("transactionDataList[" + arrayRowCount + "][1]  hat nun folgenden Inhalt: " +
                                        "(Valuta)              : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(1));
                                double Betrag =
                                        Double.parseDouble(de.adorsys.onlinebanking.web.StringUtils.shrinkString(matrixArrayData[i][4]).replaceAll("\\.", "").replaceAll(",", ".").replaceAll("&nbsp;", "").replaceAll(" ", "").replaceAll("€", "").replaceAll("&euro;", "").replaceAll("EUR", "").replaceAll("\\+", "").replaceAll("\\s", "").trim());
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(Betrag);
                                LOG.trace("transactionDataList[" + arrayRowCount + "][2]  hat nun folgenden Inhalt: " +
                                        "(Betrag)              : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(2));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(Double.parseDouble("0"));
                                LOG.trace("transactionDataList[" + arrayRowCount + "][3]  hat nun folgenden Inhalt: " +
                                        "(Saldo)               : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(3));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String("EUR"));
                                LOG.trace("transactionDataList[" + arrayRowCount + "][4]  hat nun folgenden Inhalt: " +
                                        "(Währung)             : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(4));
                                if (matrixArrayData[i][3].indexOf("zinsen") >= 0) {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add("Zinsen vor Steuern");
                                } else if (String.valueOf(Betrag).indexOf("-") == -1) {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add("Zahlung / Überweisung");
                                } else {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add("Abrechnung");
                                }

                                LOG.trace("transactionDataList[" + arrayRowCount + "][5]  hat nun folgenden Inhalt: " +
                                        "(Umsatzart)           : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(5));
                                if (!StringUtils.isBlank(matrixArrayData[i][3])) {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add(de.adorsys.onlinebanking.web.StringUtils.shrinkString(matrixArrayData[i][3].replaceAll("\\<.*?\\>", "")));
                                } else {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                }

                                LOG.trace("transactionDataList[" + arrayRowCount + "][6]  hat nun folgenden Inhalt: " +
                                        "(Verwendungszweck)    : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(6));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][7]  hat nun folgenden Inhalt: " +
                                        "(IBAN)                : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(7));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][8]  hat nun folgenden Inhalt: " +
                                        "(BIC)                 : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(8));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][9]  hat nun folgenden Inhalt: " +
                                        "(Gegenkonto-Name)     : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(9));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][10] hat nun folgenden Inhalt: " +
                                        "(Gegenkonto-Nummer)   : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(10));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][11] hat nun folgenden Inhalt: " +
                                        "(Gegenkonto-BLZ)      : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(11));
                                if (!StringUtils.isBlank(matrixArrayData[i][5])) {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add("Ursprünglicher Betrag: "
                                            + matrixArrayData[i][5]);
                                } else {
                                    ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                }

                                LOG.trace("transactionDataList[" + arrayRowCount + "][12] hat nun folgenden Inhalt: " +
                                        "(Kommentar)           : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(12));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][13] hat nun folgenden Inhalt: " +
                                        "(Primanotakennzeichen): " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(13));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(new String());
                                LOG.trace("transactionDataList[" + arrayRowCount + "][14] hat nun folgenden Inhalt: " +
                                        "(Kundenreferenz)      : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(14));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(i + 1);
                                LOG.trace("transactionDataList[" + arrayRowCount + "][15] hat nun folgenden Inhalt: " +
                                        "(Zeilennummer-Auszug) : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(15));
                                ((ArrayList) transactionDataList.get(arrayRowCount)).add(Boolean.FALSE);
                                LOG.trace("transactionDataList[" + arrayRowCount + "][16] hat nun folgenden Inhalt: " +
                                        "(vorhanden? Marker)   : " + ((ArrayList) transactionDataList.get(arrayRowCount)).get(16));
                                ++arrayRowCount;
                            } catch (Exception var45) {
                                throw new Exception("Exception beim Verarbeiten/Auslesen des RAW-Kontoauszugs-Array -" +
                                        " Log-Eintrag: " + ExceptionUtils.getStackTrace(var45));
                            }
                        }
                    }
                }

                ArrayList sortetTransactionDataList = StatementWorker.sortBankArrayList(transactionDataList, true);
                ArrayList sortetBankArrayListAndData = StatementWorker.createBankArrayListHeader("DKB", kontonummer,
                        amount, amount, 0.0D, fromDateSimple, toDateSimple);
                sortetBankArrayListAndData.addAll(sortetTransactionDataList);
                LOG.trace("Kontoauszug (ArrayList Mashup-Version): (schon fertig formatiert und sortiert):\n" + de.adorsys.onlinebanking.web.StringUtils.formatedArrayListForLog(sortetBankArrayListAndData));
                return sortetBankArrayListAndData;
            }
        } else {
            throw new Exception("Kontoauszug abholen fehlgeschlagen! Beinhaltet falsche Daten. Bitte neu versuchen " +
                    "oder überprüfen Sie dies mit einem manuellen Download auf der Bank-Homepage");
        }
    }

    private static String goToCsvPage(WebClient syncWebClient) throws Exception {
        LOG.debug("CSV laden: " + "https://www.dkb.de/banking/finanzstatus/kreditkartenumsaetze?$event=csvExport");
        Page csv = syncWebClient.getPage("https://www.dkb.de/banking/finanzstatus/kreditkartenumsaetze?$event" +
                "=csvExport");
        String csvResponse = csv.getWebResponse().getContentAsString();
        URL csvURL = csv.getUrl();
        LOG.debug("getCsvURL: csvURL: " + csvURL.toString());
        WebUtils.checkResponse(csvResponse, csv, "");
        return csvResponse;
    }

    private static void submitSearch(HtmlPage creditCardPage) throws Exception {
        DomElement searchbutton = findSearchButton(creditCardPage);
        creditCardPage = searchbutton.click();

        LOG.debug("getCsvURL: pageURL: " + creditCardPage.getUrl().toString());
        WebUtils.checkResponse(creditCardPage.asXml(), creditCardPage, "DataPageAfterSearch");
    }

    private static void selectDateRange(String fromDateSimple, String toDateSimple, HtmlPage creditCardPage) {
        ((HtmlRadioButtonInput) creditCardPage.getFirstByXPath("//input[contains(@id,'DATE_RANGE')]")).setChecked(true);
        ((HtmlInput) creditCardPage.getElementByName("postingDate")).setValueAttribute(fromDateSimple);
        ((HtmlInput) creditCardPage.getElementByName("toPostingDate")).setValueAttribute(toDateSimple);
    }

    private static HtmlPage selectTransactionStatus(HtmlPage creditCardPage) throws IOException {
        HtmlSelect statusSelect = creditCardPage.getElementByName("slTransactionStatus");
        statusSelect.getOptionByValue("0").setSelected(true);
        creditCardPage = statusSelect.getOptionByValue("0").click();
        return creditCardPage;
    }

    private static HtmlPage goToCreditCardPage(WebClient syncWebClient, HtmlPage ActiveContent) throws Exception {
        LOG.debug("AcitveContent: " + ActiveContent);
        URL activeContentUrl = ActiveContent.getUrl();
        LOG.debug("activeContentUrl: " + activeContentUrl.toString());
        String creditCardPageUrl;
        if (activeContentUrl.toString().indexOf("/portal") != -1) {
            LOG.debug("Somit wurde die (alte-)neue Portal-Version geladen");
            creditCardPageUrl = "https://banking.dkb.de/banking" + ActiveContent.getAnchorByText("Kreditkartenumsätze"
            ).getHrefAttribute();

        } else if (activeContentUrl.toString().indexOf("/banking") != -1) {
            LOG.debug("Somit wurde die neue Banking-Version geladen (muss mit JS)");
            if (!syncWebClient.getOptions().isJavaScriptEnabled()) {
                syncWebClient.getOptions().setJavaScriptEnabled(true);
                LOG.debug("syncWebClient JS-Support enabled: " + syncWebClient.getOptions().isJavaScriptEnabled());
            }

            creditCardPageUrl = "https://www.dkb.de/banking/finanzstatus/kontoumsaetze?$event=init";
        } else {
            if (activeContentUrl.toString().indexOf("/dkb") == -1) {
                throw new RuntimeException("Kreditkartenübersicht kann aufgrund einer unbekannten URL nicht " +
                        "aufgerufen werden. Bitte informieren Sie den Entwickler");
            }

            LOG.debug("Somit wurde die alte Version geladen (kann ohne JS)");
            creditCardPageUrl = "https://www.dkb.de/banking/finanzstatus/kontoumsaetze?$event=init";
        }

        LOG.debug("GET " + creditCardPageUrl.toString());

        HtmlPage KkPage = syncWebClient.getPage(creditCardPageUrl);
        LOG.debug("KkPage (Seite): " + KkPage);
        LOG.debug("pageURL: " + KkPage.getUrl().toString());

        WebUtils.checkResponse(KkPage.asXml(), KkPage, "DataPage");
        return KkPage;
    }

    private static void selectCreditCard(HtmlPage creditCardPage, String creditCardNumber) {
        URL creditCardPageUrl = creditCardPage.getUrl();

        HtmlSelect kkselect = null;

        HtmlForm form;
        if (creditCardPageUrl.toString().indexOf("/banking") != -1) {
            LOG.debug("Somit wurde die neue Banking-Version geladen (muss mit JS)");
            form = creditCardPage.getForms().get(2);
            LOG.debug("forms der Seite (zur Analyse): " + form);
            kkselect = creditCardPage.getElementByName("slAllAccounts");
            LOG.debug("kkselect: " + kkselect);

        } else {
            if (creditCardPageUrl.toString().indexOf("/dkb") == -1) {
                throw new RuntimeException("Kreditkartenauswahl-Formular kann aufgrund einer unbekannten URL nicht " +
                        "gesetzt werden. Bitte informieren Sie den Entwickler");
            }

            LOG.debug("Somit wurde die alte Version geladen (kann ohne JS)");
            form = creditCardPage.getForms().get(2);
            LOG.debug("forms der Seite (zur Analyse): " + form);
            kkselect = creditCardPage.getElementByName("slCreditCard");
            LOG.debug("kkselect: " + kkselect);
        }

        List<HtmlOption> list = kkselect.getOptions();

        boolean found = false;
        for (int i = 0; i < list.size(); ++i) {
            HtmlOption kkoption = list.get(i);
            if (kkoption.asText().substring(0, 16).contains(creditCardNumber)) {
                LOG.trace("Kreditkartenauswahl auf " + kkoption);
                kkoption.setSelected(true);
                found = true;
            }
        }
        if (!found) {
            throw new RuntimeException("Kreditkarte " + creditCardNumber + " in der Auswahl auf der Hompage nicht " +
                    "gefunden! Kontrollieren Sie bitte Ihre Angaben in den Konto-Einstellungen");
        }
    }

    private static DomElement findSearchButton(HtmlPage creditCardPage) {
        URL creditCardPageUrl = creditCardPage.getUrl();
        DomElement searchbutton;

        if (creditCardPageUrl.toString().indexOf("/banking") != -1) {
            searchbutton = creditCardPage.getFirstByXPath("//button[@id='searchbutton']");
            if (searchbutton == null) {
                searchbutton = creditCardPage.getElementByName("$$event_search");
            }

            if (searchbutton == null) {
                LOG.trace("... bei der Seite mit dem Such-Button handelt es sich um diese im XML-Format: \n" + creditCardPage.asXml());
                throw new RuntimeException("Fehler beim Setzen des Suchbutton (dieser ist NULL - Bitte den Entwickler" +
                        " im Forum informieren)");
            }
        } else {
            if (creditCardPageUrl.toString().indexOf("/dkb") == -1) {
                throw new RuntimeException("Kreditkartenauswahl-Formular kann aufgrund einer unbekannten URL nicht " +
                        "gesetzt werden. Bitte informieren Sie den Entwickler");
            }

            LOG.debug("Somit wurde die alte Version geladen (kann ohne JS)");
            searchbutton = creditCardPage.getElementByName("$$event_search");
            if (searchbutton == null) {
                LOG.trace("... bei der Seite mit dem Such-Button handelt es sich um diese im XML-Format: \n" + creditCardPage.asXml());
                throw new RuntimeException("Fehler beim Setzen des Suchbutton (dieser ist NULL - Bitte den Entwickler" +
                        " im Forum informieren)");
            }
        }
        return searchbutton;
    }

    private static void validateCreditCardNumber(String unterkonto) {
        boolean valid = false;
        if (unterkonto.length() == 16 && unterkonto.substring(4, 12).equals("********")) {
            try {
                Float.parseFloat(unterkonto.substring(0, 4));
                Float.parseFloat(unterkonto.substring(12, 16));
                valid = true;
            } catch (NumberFormatException var58) {
                valid = false;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("Die übergebene Kreditkartennummer ist nicht im erforderlichen Format " +
                    "angegeben: 1234********5678 (ersten 4 Stellen + 8 Sternchen + letzen 4 Stellen)");
        }
    }
}
