package de.adorsys.onlinebanking.web;

import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.opencsv.CSVReader;
import de.adorsys.onlinebanking.web.dkb.WebAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);
    private static final String LogIdent = "[WebSync:StringUtils] ";

    public StringUtils() {
    }

    public static String shrinkWhitespaces(String item) {
        item = item.trim();
        item = item.replaceAll("^ +", "");
        item = item.replaceAll(" +", " ");
        item = item.replaceAll(" +$", "");
        return item;
    }

    public static String shrinkString(String item) {
        item = item.trim();
        item = item.replaceAll("\\s+", " ");
        item = item.replaceAll("\\t\\n\\x0B\\f\\r", "");
        return item;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNullOrEmptyOrNothing(String str) {
        return str == null || str.length() == 0 || str.matches("\\s+");
    }

    public static String HEXtoChar(String original) {
        String StringfromHEX = original.replaceAll("\\\\", "").replaceAll("x1E9E", "ß");
        StringfromHEX = StringfromHEX.replaceAll("&#x20;", " ");
        StringfromHEX = StringfromHEX.replaceAll("x20", " ");
        StringfromHEX = StringfromHEX.replaceAll("&#x21;", "!");
        StringfromHEX = StringfromHEX.replaceAll("x21", "!");
        StringfromHEX = StringfromHEX.replaceAll("&#x2c;", ",");
        StringfromHEX = StringfromHEX.replaceAll("x2c", ",");
        StringfromHEX = StringfromHEX.replaceAll("&#x2d;", "-");
        StringfromHEX = StringfromHEX.replaceAll("x2d", "-");
        StringfromHEX = StringfromHEX.replaceAll("&#x2e;", ".");
        StringfromHEX = StringfromHEX.replaceAll("x2e", ".");
        StringfromHEX = StringfromHEX.replaceAll("&#xC4;", "Ä");
        StringfromHEX = StringfromHEX.replaceAll("xC4", "Ä");
        StringfromHEX = StringfromHEX.replaceAll("&#xE4;", "ä");
        StringfromHEX = StringfromHEX.replaceAll("xE4", "ä");
        StringfromHEX = StringfromHEX.replaceAll("&#xD6;", "Ö");
        StringfromHEX = StringfromHEX.replaceAll("xD6", "Ö");
        StringfromHEX = StringfromHEX.replaceAll("&#xF6;", "ö");
        StringfromHEX = StringfromHEX.replaceAll("xF6", "ö");
        StringfromHEX = StringfromHEX.replaceAll("&#xDC;", "Ü");
        StringfromHEX = StringfromHEX.replaceAll("xDC", "Ü");
        StringfromHEX = StringfromHEX.replaceAll("&#xFC;", "ü");
        StringfromHEX = StringfromHEX.replaceAll("xFC", "ü");
        StringfromHEX = StringfromHEX.replaceAll("&#xDF;", "ß");
        StringfromHEX = StringfromHEX.replaceAll("xDF", "ß");
        StringfromHEX = StringfromHEX.replaceAll("&#x1e9e;", "ß");
        StringfromHEX = StringfromHEX.replaceAll("x1e9e", "ß");
        return StringfromHEX;
    }

    public static double MathRound(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        } else {
            long factor = (long) Math.pow(10.0D, (double) places);
            value *= (double) factor;
            long tmp = Math.round(value);
            return (double) tmp / (double) factor;
        }
    }

    public static ArrayList<HtmlRadioButtonInput> deepCopyArrayListWithHtmlRadioButtonInputs(ArrayList<HtmlRadioButtonInput> orig) throws Exception {
        ArrayList copy = new ArrayList();

        for (int i = 0; i < orig.size(); ++i) {
            copy.add(((HtmlRadioButtonInput) orig.get(i)).cloneNode(true));
        }

        return copy;
    }

    public static ArrayList<?> deepCopyArrayList(ArrayList<?> orig) throws Exception {
        ArrayList copy = new ArrayList();

        for (int i = 0; i < orig.size(); ++i) {
            copy.add(deepCopyObject(orig.get(i)));
        }

        return copy;
    }

    public static ArrayList<ArrayList> deepCopyMatrixArrayList(ArrayList<ArrayList> orig) throws Exception {
        ArrayList copy = new ArrayList();

        for (int i = 0; i < orig.size(); ++i) {
            copy.add(new ArrayList());

            for (int j = 0; j < ((ArrayList) orig.get(i)).size(); ++j) {
                ((ArrayList) copy.get(i)).add(deepCopyObject(((ArrayList) orig.get(i)).get(j)));
            }
        }

        return copy;
    }

    public static Object deepCopyObject(Object oldObj) throws Exception {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        Object var5;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(oldObj);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            var5 = ois.readObject();
        } catch (Exception var9) {
            StringWriter sw = new StringWriter();
            var9.printStackTrace(new PrintWriter(sw));
            System.out.println("Exception in ObjectCloner = " + sw.toString());
            throw new Exception("Exception in ObjectCloner: " + var9.getMessage());
        } finally {
            oos.close();
            ois.close();
        }

        return var5;
    }

    public static boolean equalLists(ArrayList<?> one, ArrayList<?> two) {
        if (one == null && two == null) {
            return true;
        } else {
            return (one != null || two == null) && (one == null || two != null) && one.size() == two.size() ? one.equals(two) : false;
        }
    }

    public static String formatedArrayListForLog(ArrayList logArray) {
        DecimalFormat df = new DecimalFormat("0000");
        String LogListe = new String();

        for (int i = 0; i < logArray.size(); ++i) {
            LogListe = LogListe + "| " + df.format((long) i) + " |  " + logArray.get(i).toString() + "\n";
        }

        return LogListe;
    }

    public static String[] csvfile2array(Reader dataString, char strDelimiter) {
        CSVReader reader = new CSVReader(dataString, strDelimiter);
        String[] nextLine = null;

        try {
            while ((nextLine = reader.readNext()) != null) {
                System.out.println(nextLine[0] + nextLine[1] + "etc...");
            }
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return nextLine;
    }

    private static String readFile(String fileLoc) throws IOException {
        FileReader fr = new FileReader(fileLoc);
        BufferedReader br = new BufferedReader(fr);
        String data = br.readLine();

        StringBuffer buff;
        for (buff = new StringBuffer(); data != null; data = br.readLine()) {
            buff.append(data + System.getProperty("line.separator"));
        }

        br.close();
        fr.close();
        return buff.toString();
    }

    public static String[][] data2matrixArray(String dataString, char strDelimiter) throws Exception {
        LOG.debug("[WebSync:StringUtils] Funktion data2matrixArray wurde aufgerufen ...");
        String[][] matrixStringArray = (String[][]) null;

        try {
            StringReader readerdata = new StringReader(dataString);
            CSVReader datareader = new CSVReader(readerdata, strDelimiter);
            List<String[]> matrixDataArray = datareader.readAll();
            LOG.debug("[WebSync:StringUtils] matrixDataArray: " + matrixDataArray);

            try {
                matrixStringArray = new String[matrixDataArray.size()][];

                for (int arrayrowcount = 0; arrayrowcount < matrixDataArray.size(); ++arrayrowcount) {
                    String[] nextLine = (String[]) matrixDataArray.get(arrayrowcount);
                    LOG.trace("[WebSync:StringUtils] DataReader-NextLine Nr. " + arrayrowcount + " enthält das String-Array: " + Arrays.toString(nextLine));
                    matrixStringArray[arrayrowcount] = new String[nextLine.length];

                    for (int arraycolumncount = 0; arraycolumncount < nextLine.length; ++arraycolumncount) {
                        matrixStringArray[arrayrowcount][arraycolumncount] = nextLine[arraycolumncount];
                    }
                }

                datareader.close();
            } catch (Exception var13) {
                LOG.debug("[WebSync:StringUtils] Fehler beim Zusammenfügen der Zeilen des Kontoabrufs in ein Matrix-Array: " + var13);
            }
        } catch (Exception var14) {
            LOG.debug("[WebSync:StringUtils] Allgemeiner Fehler beim Umwandeln des Kontoabrufs in ein Array: " + var14.getStackTrace().toString());
        }

        return matrixStringArray;
    }

    public static String extractUrl(String text) {
        String containedUrl = null;
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, 2);

        for (Matcher urlMatcher = pattern.matcher(text); urlMatcher.find(); containedUrl = text.substring(urlMatcher.start(0), urlMatcher.end(0))) {
            ;
        }

        return containedUrl;
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, 2);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public static String createMD5Hash(String identString, Class<?> ExternalLogger, Class<?> ExternalProgressMonitor) throws NoSuchAlgorithmException, Exception {
        Method LogDebug = ExternalLogger.getMethod("debug", String.class);
        MessageDigest HashAlgorithm = MessageDigest.getInstance("MD5");
        HashAlgorithm.reset();
        HashAlgorithm.update(identString.getBytes());
        byte[] digest = HashAlgorithm.digest();
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < digest.length; ++i) {
            stringBuffer.append(Integer.toString((digest[i] & 255) + 256, 16).substring(1));
        }

        LOG.debug("[WebSync:StringUtils] createMD5Hash digested: " + digest);
        LOG.debug("[WebSync:StringUtils] createMD5Hash digested(hex): " + stringBuffer.toString());
        String MD5hash = stringBuffer.toString();
        return MD5hash;
    }

    public static boolean isIbanCountryStructure(String getString) {
        String checkString = shrinkString(getString);
        if (checkString.length() == 27 && checkString.startsWith("EG")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("AL")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("DZ")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("AD")) {
            return true;
        } else if (checkString.length() == 25 && checkString.startsWith("AO")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("AZ")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("BH")) {
            return true;
        } else if (checkString.length() == 16 && checkString.startsWith("BE")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("BJ")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("BA")) {
            return true;
        } else if (checkString.length() == 29 && checkString.startsWith("BR")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("VG")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("BG")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("BF")) {
            return true;
        } else if (checkString.length() == 16 && checkString.startsWith("BI")) {
            return true;
        } else if (checkString.length() == 19 && checkString.startsWith("CR")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("CI")) {
            return true;
        } else if (checkString.length() == 18 && checkString.startsWith("DK")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("DE")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("DO")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("EE")) {
            return true;
        } else if (checkString.length() == 18 && checkString.startsWith("FO")) {
            return true;
        } else if (checkString.length() == 18 && checkString.startsWith("FI")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("FR")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("GA")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("GE")) {
            return true;
        } else if (checkString.length() == 23 && checkString.startsWith("GI")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("GR")) {
            return true;
        } else if (checkString.length() == 18 && checkString.startsWith("GL")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("GT")) {
            return true;
        } else if (checkString.length() == 26 && checkString.startsWith("IR")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("IE")) {
            return true;
        } else if (checkString.length() == 26 && checkString.startsWith("IS")) {
            return true;
        } else if (checkString.length() == 23 && checkString.startsWith("IL")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("IT")) {
            return true;
        } else if (checkString.length() == 30 && checkString.startsWith("JO")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("CM")) {
            return true;
        } else if (checkString.length() == 25 && checkString.startsWith("CV")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("KZ")) {
            return true;
        } else if (checkString.length() == 29 && checkString.startsWith("QA")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("CG")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("XK")) {
            return true;
        } else if (checkString.length() == 21 && checkString.startsWith("HR")) {
            return true;
        } else if (checkString.length() == 30 && checkString.startsWith("KW")) {
            return true;
        } else if (checkString.length() == 21 && checkString.startsWith("LV")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("LB")) {
            return true;
        } else if (checkString.length() == 21 && checkString.startsWith("LI")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("LT")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("LU")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("MG")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("ML")) {
            return true;
        } else if (checkString.length() == 31 && checkString.startsWith("MT")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("MR")) {
            return true;
        } else if (checkString.length() == 30 && checkString.startsWith("MU")) {
            return true;
        } else if (checkString.length() == 19 && checkString.startsWith("MK")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("MD")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("MC")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("ME")) {
            return true;
        } else if (checkString.length() == 25 && checkString.startsWith("MZ")) {
            return true;
        } else if (checkString.length() == 18 && checkString.startsWith("NL")) {
            return true;
        } else if (checkString.length() == 15 && checkString.startsWith("NO")) {
            return true;
        } else if (checkString.length() == 20 && checkString.startsWith("AT")) {
            return true;
        } else if (checkString.length() == 23 && checkString.startsWith("TL")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("PK")) {
            return true;
        } else if (checkString.length() == 29 && checkString.startsWith("PS")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("PL")) {
            return true;
        } else if (checkString.length() == 25 && checkString.startsWith("PT")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("RO")) {
            return true;
        } else if (checkString.length() == 27 && checkString.startsWith("SM")) {
            return true;
        } else if (checkString.length() == 25 && checkString.startsWith("ST")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("SA")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("SE")) {
            return true;
        } else if (checkString.length() == 21 && checkString.startsWith("CH")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("SN")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("RS")) {
            return true;
        } else if (checkString.length() == 31 && checkString.startsWith("SC")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("SK")) {
            return true;
        } else if (checkString.length() == 19 && checkString.startsWith("SI")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("ES")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("CZ")) {
            return true;
        } else if (checkString.length() == 24 && checkString.startsWith("TN")) {
            return true;
        } else if (checkString.length() == 26 && checkString.startsWith("TR")) {
            return true;
        } else if (checkString.length() == 29 && checkString.startsWith("UA")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("HU")) {
            return true;
        } else if (checkString.length() == 23 && checkString.startsWith("AE")) {
            return true;
        } else if (checkString.length() == 22 && checkString.startsWith("GB")) {
            return true;
        } else if (checkString.length() == 28 && checkString.startsWith("CY")) {
            return true;
        } else {
            return checkString.length() == 27 && checkString.startsWith("CF");
        }
    }

    public static class MatrixArrayListDateComparator implements Comparator {
        public MatrixArrayListDateComparator() {
        }

        public int compare(Object obj1, Object obj2) {
            if (obj1 instanceof ArrayList && obj2 instanceof ArrayList) {
                Date date1 = (Date) ((ArrayList) obj1).get(0);
                Date date2 = (Date) ((ArrayList) obj2).get(0);
                return date2.compareTo(date1);
            } else {
                throw new ClassCastException("compared objects must be instances of ArrayList");
            }
        }
    }
}
