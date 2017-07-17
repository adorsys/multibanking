package domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description = "BankAccess account information.", value = "BankAccess")
public class BankAccess {

    @ApiModelProperty(value = "The bank name", example = "Deutsche Bank")
    private String bankName;
    @ApiModelProperty(value = "The bank login", required = true, example = "1234567890")
    private String bankLogin;
    @ApiModelProperty(value = "The 2nd bank login id", example = "1234567890")
    private String bankLogin2;
    @ApiModelProperty(value = "The bank code", required = true, example = "76070024")
    private String bankCode;
    @ApiModelProperty(hidden = true)
    private String hbciPassportState;
    @ApiModelProperty(hidden = true)
    private Map<BankApi, String> externalIdMap = new HashMap<>();

    public BankAccess externalId(BankApi bankApi, String externalId) {
        externalIdMap.put(bankApi, externalId);
        return this;
    }

}
