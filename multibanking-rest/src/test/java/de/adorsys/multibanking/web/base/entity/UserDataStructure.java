package de.adorsys.multibanking.web.base.entity;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by peter on 07.05.18 at 19:34.
 */
public class UserDataStructure {
    private static final String DateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss";
    private final static Logger LOGGER = LoggerFactory.getLogger(UserDataStructure.class);

    private final JSONObject root;

    public UserDataStructure(JSONObject root) {
        this.root = root;
    }

    public List<BankAccessID> getBankAccessIDs() {
        try {
            List<BankAccessID> list = new ArrayList<>();
            for (BankAccessWrapperJson bankAccessWrapperJson : getBankAccessWrapperList()) {
                BankAccessJson bankAccessJson = getBankAccessObject(bankAccessWrapperJson);
                list.add(new BankAccessID(bankAccessJson.get().getString("id")));
            }
            ;
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public List<BankAccountID> getBankAccountIDs(BankAccessID bankAccessID) {
        try {
            List<BankAccountID> list = new ArrayList<>();
            for (BankAccountJson bankAccountJson : getBankAccountObjects(bankAccessID)) {
                list.add(new BankAccountID(bankAccountJson.get().getString("id")));
            }
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }

    public Optional<SyncStatus> getSyncStatus(BankAccessID bankAccessID, BankAccountID bankAccountID) {
        try {
            String syncStatusString = getBankAccountObject(getBankAccountWrapperObject(bankAccessID, bankAccountID)).get().getString("syncStatus");
            LOGGER.debug(bankAccessID + " " + bankAccountID + " syncStatus:" + syncStatusString);
            if (syncStatusString == null || syncStatusString.equalsIgnoreCase("null")) {
                return Optional.empty();
            }
            return Optional.of(new SyncStatus(syncStatusString));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public Optional<Date> getLastSync(BankAccessID bankAccessID, BankAccountID bankAccountID) {
        try {
            BankAccountWrapperJson bankAccountWrapperObject = getBankAccountWrapperObject(bankAccessID, bankAccountID);
            BankAccountJson bankAccountObject = getBankAccountObject(bankAccountWrapperObject);
            String lastSyncString = bankAccountObject.get().getString("lastSync");
            if (lastSyncString == null || lastSyncString.equalsIgnoreCase("null")) {
                return Optional.empty();
            }
            return Optional.of(getDateFromString(lastSyncString));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public Optional<Date> getSyncStatusTime(BankAccessID bankAccessID, BankAccountID bankAccountID) {
        try {
            BankAccountWrapperJson bankAccountWrapperObject = getBankAccountWrapperObject(bankAccessID, bankAccountID);
            String syncStatusTimeString = bankAccountWrapperObject.get().getString("syncStatusTime");
            if (syncStatusTimeString == null || syncStatusTimeString.equalsIgnoreCase("null")) {
                return Optional.empty();
            }
            return Optional.of(getDateFromString(syncStatusTimeString));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public Optional<List<String>> getBookingPeriods(BankAccessID bankAccessID, BankAccountID bankAccountID) {
        try {
            BankAccountWrapperJson bankAccountWrapper = getBankAccountWrapperObject(bankAccessID, bankAccountID);
            String bookingFilesString = bankAccountWrapper.get().getString("bookingFiles");
            LOGGER.debug("bookingfilesstring:" + bookingFilesString);
            JSONArray bookingFiles = bankAccountWrapper.get().getJSONArray("bookingFiles");
            if (bookingFiles == null || bookingFiles.length() == 0) {
                return Optional.empty();
            }
            List<String> list = new ArrayList<>();
            for (int i = 0; i<bookingFiles.length(); i++) {
                JSONObject bookingFile = bookingFiles.getJSONObject(i);
                String period = bookingFile.getString("period");
                LOGGER.debug("KEY ist " + period);
                list.add(period);
            }
            LOGGER.debug("Das waren alle Keys");
            return Optional.of(list);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private List<BankAccessWrapperJson> getBankAccessWrapperList() {
        try {
            List<BankAccessWrapperJson> bankAccessWrapperList = new ArrayList<>();
            JSONArray bankAccesses = root.getJSONArray("bankAccesses");
            for (int i = 0; i < bankAccesses.length(); i++) {
                bankAccessWrapperList.add(new BankAccessWrapperJson(bankAccesses.getJSONObject(i)));
            }
            return bankAccessWrapperList;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private BankAccessWrapperJson getBankAccessWrapper(BankAccessID bankAccessID) {
        try {
            for (BankAccessWrapperJson bankAccessWrapper : getBankAccessWrapperList()) {
                BankAccessJson bankAccessJson = getBankAccessObject(bankAccessWrapper);
                BankAccessID bankAccessIDfound = new BankAccessID(bankAccessJson.get().getString("id"));
                if (bankAccessIDfound.equals(bankAccessID)) {
                    return bankAccessWrapper;
                }
            }
            throw new BaseException("did not find bankAccess with id " + bankAccessID + " in " + root.toString());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private BankAccessJson getBankAccessObject(BankAccessWrapperJson bankAccessWrapper) {
        try {
            return new BankAccessJson(bankAccessWrapper.get().getJSONObject("bankAccess"));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private List<BankAccountWrapperJson> getBankAccountWrapperObjects(BankAccessID bankAccessID) {
        try {
            List<BankAccountWrapperJson> bankAccountsWrapperList = new ArrayList<>();
            BankAccessWrapperJson bankAccessWrapper = getBankAccessWrapper(bankAccessID);
            JSONArray bankAccounts = bankAccessWrapper.get().getJSONArray("bankAccounts");
            for (int j = 0; j < bankAccounts.length(); j++) {
                bankAccountsWrapperList.add(new BankAccountWrapperJson(bankAccounts.getJSONObject(j)));
            }
            return bankAccountsWrapperList;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private List<BankAccountJson> getBankAccountObjects(BankAccessID bankAccessID) {
        try {
            List<BankAccountJson> bankAccountsList = new ArrayList<>();
            List<BankAccountWrapperJson> bankAccountWrapper = getBankAccountWrapperObjects(bankAccessID);
            for (int j = 0; j < bankAccountWrapper.size(); j++) {
                bankAccountsList.add(new BankAccountJson(bankAccountWrapper.get(j).get().getJSONObject("bankAccount")));
            }
            return bankAccountsList;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private BankAccountWrapperJson getBankAccountWrapperObject(BankAccessID bankAccessID, BankAccountID bankAccountID) {
        try {
            List<BankAccountWrapperJson> bankAccounts = getBankAccountWrapperObjects(bankAccessID);

            for (int j = 0; j < bankAccounts.size(); j++) {
                BankAccountWrapperJson bankAccountWrapper = bankAccounts.get(j);
                BankAccountJson bankAccountJson = getBankAccountObject(bankAccountWrapper);
                BankAccountID bankAccountIDFound = new BankAccountID(bankAccountJson.get().getString("id"));
                if (bankAccountIDFound.equals(bankAccountID)) {
                    return bankAccountWrapper;
                }
            }
            throw new BaseException("Did not find any bankAccout with id " + bankAccountID + " for bankAccessId " + bankAccessID + " in " + root.toString());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private BankAccountJson getBankAccountObject(BankAccountWrapperJson bankAccountWrapperJson) {
        try {
            return new BankAccountJson(bankAccountWrapperJson.get().getJSONObject("bankAccount"));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static class BankAccessWrapperJson {
        private final JSONObject jsonObject;

        public BankAccessWrapperJson(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JSONObject get() {
            return jsonObject;
        }
    }

    private static class BankAccessJson {
        private final JSONObject jsonObject;

        public BankAccessJson(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JSONObject get() {
            return jsonObject;
        }
    }

    private static class BankAccountWrapperJson {
        private final JSONObject jsonObject;

        public BankAccountWrapperJson(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JSONObject get() {
            return jsonObject;
        }
    }

    private static class BankAccountJson {
        private final JSONObject jsonObject;

        public BankAccountJson(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JSONObject get() {
            return jsonObject;
        }
    }

    @Override
    public String toString() {
        return formatJson(root.toString());
    }

    public static String formatJson(String content) {
        try {
            int spacesToIndentEachLevel = 4;
            return new JSONObject(content).toString(spacesToIndentEachLevel);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static Date getDateFromString(String dateString) {
        try {
            // 2018-05-12T16:22:25
            // yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormatPattern);
            return sdf.parse(dateString);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static String getStringFromLocalDate(LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return getStringFromDate(date);
    }
    public static String getStringFromDate(Date date) {
        try {
            // 2018-05-12T16:22:25
            // yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormatPattern);
            return sdf.format(date);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}


