package domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccess {

    private String bankName;
    private String bankLogin;
    private String bankCode;
    private String passportState;
    private Map<BankApi, String> externalIdMap;

    public BankAccess externalId(BankApi bankApi, String externalId) {
        if (externalIdMap == null) {
            externalIdMap = new HashMap<>();
            externalIdMap.put(bankApi, externalId);
        }
        return this;
    }

}
