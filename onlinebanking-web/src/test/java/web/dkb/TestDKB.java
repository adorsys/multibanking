package web.dkb;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.adorsys.onlinebanking.web.dkb.WebAuth;
import de.adorsys.onlinebanking.web.dkb.WebPull;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TestDKB {

    @Test
    public void testDKB() throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        HtmlPage postLogin = WebAuth.login("12324463", "3675g", webClient);

        String from = LocalDate.now().minusDays(60).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String to = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        ArrayList transData = WebPull.getTransData(webClient, "2257793", "4998********6791", from, to, postLogin);
        System.out.println(transData);

        WebAuth.logout(webClient);
    }
}
