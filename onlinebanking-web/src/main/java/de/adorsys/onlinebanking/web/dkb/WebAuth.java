package de.adorsys.onlinebanking.web.dkb;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class WebAuth {

    private static final Logger LOG = LoggerFactory.getLogger(WebAuth.class);

    public static HtmlPage login(String responseLogin, String responsePasswort, WebClient syncWebClient) throws Exception {

        Object submitLogin = goToLoginPage(responseLogin, responsePasswort, syncWebClient);

        return submitLoginForm((DomElement) submitLogin);
    }

    private static DomElement goToLoginPage(String responseLogin, String responsePasswort, WebClient syncWebClient) throws Exception {
        LOG.info("DKB-Login...");
        HtmlPage pageLogin = syncWebClient.getPage("https://banking.dkb.de/banking");
        LOG.debug("pageLogin: " + pageLogin);
        String pageLoginXML = pageLogin.asXml();
        WebUtils.checkResponse(pageLoginXML, pageLogin, "Login");

        if (pageLogin == null) {
            throw new Exception("Die Login-Seite konnte nicht aufgerufen werden!");
        }

        DomElement submitLogin = null;
        try {
            URL pageLoginUrl = pageLogin.getUrl();
            LOG.debug("LoginURL: " + pageLoginUrl.toString());
            HtmlForm formLogin;
            if (pageLoginUrl.toString().indexOf("/portal") != -1) {
                LOG.debug("Somit wurde die (alte-)neue Portal-Version geladen");
                formLogin = pageLogin.getFirstByXPath("//form[@class='anmeldung']");
                LOG.debug("formLogin: " + formLogin);
                ((HtmlInput) formLogin.getFirstByXPath("//input[@maxlength='16']")).setValueAttribute(responseLogin);
                ((HtmlInput) formLogin.getFirstByXPath("//input[@type='password']")).setValueAttribute(responsePasswort);
                submitLogin = formLogin.getInputByValue("Anmelden");
            } else if (pageLoginUrl.toString().indexOf("/banking") != -1) {
                LOG.debug("Somit wurde die neue Banking-Version geladen (muss mit JS)");
                if (!syncWebClient.getOptions().isJavaScriptEnabled()) {
                    syncWebClient.getOptions().setJavaScriptEnabled(true);
                    LOG.debug("syncWebClient JS-Support enabled: " + syncWebClient.getOptions().isJavaScriptEnabled());
                }

                formLogin = pageLogin.getFormByName("login");
                LOG.debug("formLogin: " + formLogin);
                formLogin.getInputByName("j_username").setValueAttribute(responseLogin);
                formLogin.getInputByName("j_password").setValueAttribute(responsePasswort);
                submitLogin = pageLogin.getFirstByXPath("//button[@id='buttonlogin']");
                if (submitLogin == null) {
                    submitLogin = pageLogin.getFirstByXPath("//input[@id='buttonlogin']");
                }

                LOG.debug("submitLogin: " + submitLogin);
            } else if (pageLoginUrl.toString().indexOf("/dkb") != -1) {
                LOG.debug("Somit wurde die alte Version geladen (kann ohne JS)");
                formLogin = pageLogin.getFormByName("login");
                LOG.debug("formLogin: " + formLogin);
                formLogin.getInputByName("j_username").setValueAttribute(responseLogin);
                formLogin.getInputByName("j_password").setValueAttribute(responsePasswort);
                submitLogin = pageLogin.getFirstByXPath("//input[@id='buttonlogin']");
            } else {
                if (pageLoginUrl.toString().indexOf("wartung") == -1) {
                    throw new Exception("Login-Formular kann aufgrund einer unbekannten Login-URL nicht gesetzt werden. Bitte informieren Sie den Entwickler");
                }

                WebUtils.checkResponse(pageLoginXML, pageLogin, "Login");
            }

            if (submitLogin == null) {
                LOG.trace("... bei der Seite mit dem Such-Button handelt es sich um diese im XML-Format: \n" + pageLogin.asXml());
                throw new Exception("Fehler beim Setzen des Loginbutton (dieser ist NULL - Bitte den Entwickler im Forum informieren)");
            }
        } catch (Exception var27) {
            throw new Exception("Fehler beim Setzen des Login-Formulars oder der Felder (siehe Log - Bitte den Entwickler im Forum informieren)\nLog-Eintrag: " + ExceptionUtils.getStackTrace(var27));
        }
        return submitLogin;
    }

    private static HtmlPage submitLoginForm(DomElement submitLogin) throws Exception {
        LOG.info("Login-Form wird abgesendet ...");
        HtmlPage postLoginPage = submitLogin.click();
        LOG.debug("postLoginPage: " + postLoginPage);

        String postLoginXML = postLoginPage.asXml();
        WebUtils.checkResponse(postLoginXML, postLoginPage, "Login");


        if (postLoginPage == null) {
            throw new Exception("Die Login-Folgeseite konnte nicht aufgerufen werden!");
        }
        URL postLoginURL = postLoginPage.getUrl();
        LOG.debug("postLoginURL: " + postLoginURL.toString());
        if (postLoginURL.toString().indexOf("/portal") != -1) {
            LOG.debug("Somit wurde die (alte-)neue Portal-Version geladen");
        } else if (postLoginURL.toString().indexOf("/banking") != -1) {
            LOG.debug("Somit wurde die neue Banking-Version geladen (muss mit JS)");
        } else {
            if (postLoginURL.toString().indexOf("/dkb") == -1) {
                throw new Exception("Seite nach Login kann aufgrund einer unbekannten URL nicht auf Login-Formular hin geprüft werden. Bitte informieren Sie den Entwickler");
            }

            LOG.debug("Somit wurde die alte Version geladen");
        }

        if (!postLoginXML.contains("class=\"anmeldung\"") && !postLoginXML.contains("id=\"login\"")) {
            return postLoginPage;
        } else {
            throw new Exception("Die Loginseite wird trotz keinem bekannten Fehler noch immer angezeigt. Informieren Sie bitte den Entwickler im Forum");
        }
    }


    public static void logout(WebClient syncWebClient) throws Exception {
        boolean isSelfException = false;

        try {
            String logoutURL = "https://www.dkb.de/DkbTransactionBanking/banner.xhtml?$event=logout";
            HtmlPage postLogoutPage = null;
            String postLogoutPageXML = null;
            LOG.debug("GET: " + logoutURL);

            try {
                postLogoutPage = (HtmlPage) syncWebClient.getPage(logoutURL);
                LOG.debug("PostLogoutPage: " + postLogoutPage);
            } catch (Exception var21) {
                isSelfException = true;
                throw new Exception("Der Server antwortet nicht oder es existiert keine Internertverbindung (siehe Log)\nLog-Eintrag: " + ExceptionUtils.getStackTrace(var21));
            }

            postLogoutPageXML = postLogoutPage.asXml();

            try {
                WebUtils.checkResponse(postLogoutPageXML, postLogoutPage, "Logout");
            } catch (Exception var20) {
                isSelfException = true;
                throw new Exception("Fehlermeldung des Servers: " + var20.getMessage());
            }

            if (postLogoutPageXML.contains("Sie haben sich erfolgreich abgemeldet")) {
                LOG.info("Logout bei der '" + "Deutsche Kreditbank AG (DKB)" + "' war erfolgreich");
                syncWebClient.close();
            } else {
                LOG.trace("Seite nach Logout-Aufruf unbekannt; Code zur Analyse:\n" + postLogoutPageXML);
                isSelfException = true;
                throw new Exception("Seite nach Logout-Aufruf unbekannt; Bitte dem Entwickler im Forum melden!");
            }
        } catch (Exception var22) {
            if (isSelfException) {
                throw new Exception(var22.getMessage());
            } else {
                LOG.error("WebLogout fehlerhaft! Stacktrace:\n" + ExceptionUtils.getStackTrace(var22));
                throw new Exception("WebLogout fehlerhaft! Fehlermeldung:" + var22.getMessage());
            }
        }
    }
}

