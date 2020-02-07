package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.request.AbstractRequest;
import de.adorsys.multibanking.hbci.HbciCacheHandler;
import org.kapott.hbci.callback.HBCICallback;
import org.mapstruct.Mapper;

@Mapper(imports = {HbciCacheHandler.class})
public interface HbciDialogRequestMapper {

    HbciDialogRequest toHbciDialogRequest(AbstractRequest transactionRequest, HBCICallback callback);

}
