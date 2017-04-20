package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptionSpec;

import io.swagger.annotations.ApiModel;

@ApiModel(description="HBCI load accounts request", value="HbciLoadAccountsRequest" , parent=JweEncryptionSpec.class)
public class HbciLoadAccountsRequest extends HbciBankAccessRequest {
}
