package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.request.AbstractRequest;
import org.kapott.hbci.callback.HBCICallback;
import org.mapstruct.Mapper;

@Mapper
public interface HbciDialogRequestMapper {
    HbciDialogRequest toHbciDialogRequest(AbstractRequest transactionRequest, HBCICallback callback);
}
