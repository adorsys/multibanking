package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.web.model.BankAccessTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccessMapper {

    @Mapping(target = "bankLogin", ignore = true)
    @Mapping(target = "bankLogin2", ignore = true)
    @Mapping(target = "pin", ignore = true)
    @Mapping(target = "pin2", ignore = true)
    BankAccessTO toBankAccessTO(BankAccessEntity bankAccessEntity);

    @Mapping(target = "tanTransportTypes", ignore = true)
    @Mapping(target = "hbciPassportState", ignore = true)
    @Mapping(target = "externalIdMap", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "temporary", ignore = true)
    BankAccessEntity toBankAccessEntity(BankAccessTO bankAccessTO);

}

