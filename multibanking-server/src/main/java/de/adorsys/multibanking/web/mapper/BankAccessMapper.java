package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.web.model.BankAccessTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccessMapper {

    BankAccessTO toBankAccessTO(BankAccessEntity bankAccessEntity);

    @Mapping(target = "tanTransportTypes", ignore = true)
    @Mapping(target = "hbciPassportState", ignore = true)
    @Mapping(target = "externalIdMap", ignore = true)
    @Mapping(target = "bankCode", expression = "java(org.iban4j.Iban.valueOf(bankAccessTO.getIban()).getBankCode())")
    BankAccessEntity toBankAccessEntity(BankAccessTO bankAccessTO, String userId, boolean temporary);

}

