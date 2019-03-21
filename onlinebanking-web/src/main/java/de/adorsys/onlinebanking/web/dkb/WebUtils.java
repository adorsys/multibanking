package de.adorsys.onlinebanking.web.dkb;

import com.gargoylesoftware.htmlunit.Page;
import de.adorsys.onlinebanking.web.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class WebUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WebAuth.class);

    public static String checkResponse(String responseContentAsString, Page responsePage, String siteType) throws Exception {
        String errorResponse;
        String errorMessage = null;

        if (responseContentAsString.contains("PIN ändern")) {
            errorMessage = "Es ist notwendig dass Sie Ihre Zugangs-PIN ändern. \n\nBitte melden Sie sich im Online-Baning an und folgen dort den Anweisungen.";
            errorMessage = StringUtils.shrinkString(errorMessage);
            throw new Exception(errorMessage);
        } else if (!responseContentAsString.contains("Online-Sperre aufheben") && !responseContentAsString.contains("PIN-Sperre aufheben")) {
            if (!responseContentAsString.contains("errorMessage") && !responseContentAsString.contains("remarkBox\">") && !responseContentAsString.contains("class=\"msg")) {
                if (responseContentAsString.contains("wurden automatisch vom System abgemeldet")) {
                    throw new Exception("Die Sitzung wurde von der Bank beendet. Bitte melden Sie sich erneut an");
                } else if ((!responseContentAsString.contains("Aus technischen Gründen") || !responseContentAsString.contains("leider nicht zur Verfügung")) && (!responseContentAsString.contains("us technischen Gr&uuml;nden") || !responseContentAsString.contains("leider nicht zur Verf&uuml;gung")) && (!responseContentAsString.contains("Achtung! Wichtiger Hinweis") || !responseContentAsString.contains("mit Hochdruck daran")) && (!responseContentAsString.contains("Wichtiger Hinweis") || !responseContentAsString.contains("Banking zurzeit leider nicht zur"))) {
                    if (responseContentAsString.contains("Bitte melden Sie sich erneut mit Ihrer Kontonummer und Ihrer PIN an")) {
                        throw new Exception("Die Sitzung wurde von der Bank beendet. Bitte melden Sie sich erneut an");
                    } else if (responseContentAsString.contains("Beim Online-Banking ist ein Fehler aufgetreten") && responseContentAsString.contains("Ihre Session") && responseContentAsString.contains("beendet")) {
                        throw new Exception("Beim Online-Banking ist ein Fehler aufgetreten. Ihre Session wurde möglicherweise aus Sicherheitsgründen beendet");
                    } else if (!siteType.equals("Login")) {
                        if (!responseContentAsString.contains("class=\"anmeldung\"") && !responseContentAsString.contains("id=\"login\"")) {
                            return errorMessage;
                        } else {
                            throw new Exception("Die Loginseite wird wieder angezeigt. Informieren Sie bitte den Entwickler im Forum");
                        }
                    } else {
                        return null;
                    }
                } else {
                    throw new Exception("Aus technischen Gründen steht das Internet-Banking zurzeit leider nicht zur Verfügung. Wir arbeiten mit Hochdruck daran, dass Sie sich in Kürze wieder einloggen können");
                }
            } else {
                errorResponse = responsePage.getWebResponse().getContentAsString();
                String windowErrorMessage = formErrorMessage(errorResponse);
                errorMessage = windowErrorMessage.replaceAll("\\n\\n\\n", " -- ");
                errorMessage = errorMessage.replaceAll("\\n\\n", ": ");
                errorMessage = errorMessage.replaceAll("\\n", " ");
                String[] MessageArray = errorMessage.split(" -- ");
                errorMessage = MessageArray[0];
                if (responseContentAsString.contains("class=\"msginst")) {
                    if (MessageArray.length > 1) {
                        LOG.info("Wichtige Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + MessageArray[1]);
                        LOG.info("weitere Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + errorMessage);
                    } else {
                        LOG.info("Wichtige Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + errorMessage);
                    }
                    return null;
                } else if (!responseContentAsString.contains("class=\"msgwarning") && !responseContentAsString.contains("class=\"clearfix successBox remarkBox")) {
                    if (responseContentAsString.contains("remarkBox\">")) {
                        if (siteType.equals("DataPage") && errorMessage.equals("Für den angegebenen Zeitraum sind keine Umsätze vorhanden.")) {
                            return null;
                        } else if (siteType.equals("DataPageAfterSearch")) {
                            return null;
                        } else {
                            LOG.info("Info-, oder Warnungs-Nachricht der " + "Deutsche Kreditbank AG (DKB)" + ": " + errorMessage);
                            if (MessageArray.length > 1) {
                                LOG.info("weitere Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + MessageArray[1]);
                            }

                            return null;
                        }
                    } else if (!responseContentAsString.contains("class=\"msginfo") && !responseContentAsString.contains("class=\"msgcont")) {
                        throw new Exception(errorMessage);
                    } else {
                        LOG.info("Informations-Nachricht der " + "Deutsche Kreditbank AG (DKB)" + ": " + errorMessage);
                        if (MessageArray.length > 1) {
                            LOG.info("weitere Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + MessageArray[1]);
                        }

                        return null;
                    }
                } else {
                    LOG.info("Warnungs-Nachricht der Bank: " + errorMessage);
                    if (MessageArray.length > 1) {
                        LOG.info("weitere Meldung der " + "Deutsche Kreditbank AG (DKB)" + ": " + MessageArray[1]);
                    }
                    return null;
                }
            }
        } else {
            errorMessage = "Ihr Zugang zum Online-Banking ist gesperrt. \n\nFür die Freischaltung melden Sie sich im Online-Banking an und folgen dort den Anweisungen.\n";
            errorMessage = StringUtils.shrinkString(errorMessage);
            throw new Exception(errorMessage);
        }

    }

    public static String formErrorMessage(String contentStringToParse) throws Exception {
        try {
            int ErrorIDXstart;
            int ErrorTextIDXend;
            String ErrorText;
            int ErrorTextIDXStart;
            if (!contentStringToParse.contains("errorMessage") && !contentStringToParse.contains("remarkBox\">")) {
                if (!contentStringToParse.contains("class=\"msg")) {
                    throw new Exception("Fehlernachrichten konnten nun doch nicht gefunden werden?! Somit kein auslesen möglich");
                }

                ErrorIDXstart = contentStringToParse.indexOf("class=\"msg");
                LOG.debug("ErrorIDXstart: " + ErrorIDXstart);
                ErrorTextIDXStart = contentStringToParse.indexOf(">", ErrorIDXstart) + 1;
                LOG.debug("ErrorTextIDXStart: " + ErrorTextIDXStart);
                ErrorTextIDXend = contentStringToParse.indexOf("</div>", ErrorTextIDXStart);
                LOG.debug("ErrorTextIDXend: " + ErrorTextIDXend);
                ErrorText = contentStringToParse.substring(ErrorTextIDXStart, ErrorTextIDXend);
                LOG.trace("ErrorText (unformatiert): " + ErrorText);
                LOG.debug("Suche eine weitere (versteckte) Nachricht ...");
                int ErrorIDXstart2 = contentStringToParse.lastIndexOf("class=\"msg");
                LOG.debug("ErrorIDXstart2: " + ErrorIDXstart2);
                if (ErrorIDXstart2 > ErrorIDXstart) {
                    ErrorTextIDXStart = contentStringToParse.indexOf(">", ErrorIDXstart2) + 1;
                    LOG.debug("ErrorTextIDXStart: " + ErrorTextIDXStart);
                    ErrorTextIDXend = contentStringToParse.indexOf("</div>", ErrorTextIDXStart);
                    LOG.debug("ErrorTextIDXend: " + ErrorTextIDXend);
                    String ErrorText2 = contentStringToParse.substring(ErrorTextIDXStart, ErrorTextIDXend);
                    LOG.trace("ErrorText2 (unformatiert): " + ErrorText2);
                    ErrorText = ErrorText + "\n\n\n" + ErrorText2;
                } else {
                    LOG.debug("... anscheinend keine weitere vorhanden");
                }
            } else {
                ErrorIDXstart = contentStringToParse.indexOf("errorMessage");
                if (ErrorIDXstart == -1) {
                    ErrorIDXstart = contentStringToParse.indexOf("remarkBox\">");
                }

                LOG.debug("ErrorIDXstart: " + ErrorIDXstart);
                ErrorTextIDXStart = contentStringToParse.indexOf("</span>", ErrorIDXstart) + 7;
                LOG.debug("ErrorTextIDXStart: " + ErrorTextIDXStart);
                ErrorTextIDXend = contentStringToParse.indexOf("</li>", ErrorTextIDXStart);
                LOG.debug("ErrorTextIDXend: " + ErrorTextIDXend);
                ErrorText = contentStringToParse.substring(ErrorTextIDXStart, ErrorTextIDXend);
                LOG.trace("ErrorText (unformatiert): " + ErrorText);
            }

            String formErrorText = ErrorText.replaceAll("</strong><br/>", "\n\n");
            formErrorText = formErrorText.replaceAll("</strong><br />", "\n\n");
            formErrorText = formErrorText.replaceAll("</span></h3>", "\n\n");
            formErrorText = formErrorText.replaceAll("<br/>", "\n");
            formErrorText = formErrorText.replaceAll("<br />", "\n");
            formErrorText = formErrorText.replaceAll("</li><li>", "\n");
            formErrorText = StringEscapeUtils.unescapeHtml4(formErrorText);
            formErrorText = formErrorText.replaceAll("\\<.*?\\>", "");
            formErrorText = StringUtils.shrinkWhitespaces(formErrorText);
            formErrorText = formErrorText.trim();
            LOG.debug("ErrorText (formatiert): " + formErrorText);
            return formErrorText;
        } catch (Exception var13) {
            StringWriter sw = new StringWriter();
            var13.printStackTrace(new PrintWriter(sw));
            LOG.error("formErrorMessage fehlerhaft:\n" + sw.toString());
            throw new Exception("formErrorMessage fehlerhaft: " + var13.getMessage());
        }
    }
}
