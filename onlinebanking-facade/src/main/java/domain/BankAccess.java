package domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description = "BankAccess account information", value = "BankAccess")
public class BankAccess {

    @ApiModelProperty(value = "Bank name", example = "Deutsche Bank")
    private String bankName;
    @ApiModelProperty(value = "Bank login name", required = true, example = "l.name")
    private String bankLogin;
    @ApiModelProperty(value = "2nd bank login name", example = "1234567890")
    private String bankLogin2;
    @ApiModelProperty(value = "Bank code", required = true, example = "76070024")
    private String bankCode;
    @ApiModelProperty(value = "Supported tan transport types", example = "iTAN")
    List<TanTransportType> tanTransportTypes;
    @ApiModelProperty(hidden = true)
    private String hbciPassportState;
    @ApiModelProperty(hidden = true)
    private Map<BankApi, String> externalIdMap = new HashMap<>();

    public BankAccess externalId(BankApi bankApi, String externalId) {
        externalIdMap.put(bankApi, externalId);
        return this;
    }

}
