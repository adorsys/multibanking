package domain;

import io.swagger.annotations.ApiModel;

/**
 * Created by alexg on 17.05.17.
 */
@ApiModel(description="The banking access backend", value="BankApi" )
public enum BankApi {
    HBCI, FIGO, FINAPI, XS2A, MOCK, FIGO_ALTERNATIVE, SCREEN_SCRAPPING
}
