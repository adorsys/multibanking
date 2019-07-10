package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.Contract;
import de.adorsys.multibanking.web.model.AnalyticsTO;
import de.adorsys.multibanking.web.model.ContractTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    AnalyticsTO toAnalyticsTO(AccountAnalyticsEntity analyticsEntity);

    @Mapping(target = "id", ignore = true)
    ContractTO toContractTO(Contract contract);

}
