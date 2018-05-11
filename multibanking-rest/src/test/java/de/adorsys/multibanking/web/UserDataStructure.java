package de.adorsys.multibanking.web;

import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 07.05.18 at 19:34.
 */
public class UserDataStructure {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserDataStructure.class);

    private final JSONObject root;
    public UserDataStructure(JSONObject root) {
        this.root = root;
    }

    public List<BankAccessID> getBankAccessIDs() {
        try {
            List<BankAccessID> list = new ArrayList<>();
            JSONObject userEntity = root.getJSONObject("userEntity");
            String userId = userEntity.getString("id");

            JSONArray bankAccesses = root.getJSONArray("bankAccesses");
            for (int i = 0; i < bankAccesses.length(); i++) {
                JSONObject bankAccessWrapper = bankAccesses.getJSONObject(i);
                JSONObject bankAccess = bankAccessWrapper.getJSONObject("bankAccess");
                Assert.assertEquals(bankAccess.getString("userId"), userId);
                list.add(new BankAccessID(bankAccess.getString("id")));

            }
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public List<BankAccountID> getBankAccountIDs(BankAccessID bankAccessID) {
        try {
            JSONObject userEntity = root.getJSONObject("userEntity");
            String userId = userEntity.getString("id");

            JSONArray bankAccesses = root.getJSONArray("bankAccesses");
            for (int i = 0; i < bankAccesses.length(); i++) {
                JSONObject bankAccessWrapper = bankAccesses.getJSONObject(i);
                JSONObject bankAccess = bankAccessWrapper.getJSONObject("bankAccess");
                Assert.assertEquals(bankAccess.getString("userId"), userId);
                BankAccessID bankAccessIDfound = new BankAccessID(bankAccess.getString("id"));
                if (bankAccessID.equals(bankAccessIDfound)) {
                    List<BankAccountID> list = new ArrayList<>();
                    JSONArray bankAccounts = bankAccessWrapper.getJSONArray("bankAccounts");
                    for (int j = 0; j<bankAccounts.length(); j++) {
                        JSONObject bankAccount = bankAccounts.getJSONObject(j).getJSONObject("bankAccount");
                        String id = bankAccount.getString("id");
                        list.add(new BankAccountID(id));
                    }
                    return list;
                }
            }
            throw new BaseException("Did not find any bankAccess with id " + bankAccessID + " in " + root.toString());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
