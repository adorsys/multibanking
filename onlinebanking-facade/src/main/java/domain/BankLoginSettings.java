package domain;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alexg on 14.07.17.
 */
@Data
public class BankLoginSettings {

    private String bank_name;
    private boolean supported;
    private String icon;
    private HashMap<String, String> additional_icons;
    private List<BankLoginCredential> credentials;
    private String auth_type;
    private String advice;
}
