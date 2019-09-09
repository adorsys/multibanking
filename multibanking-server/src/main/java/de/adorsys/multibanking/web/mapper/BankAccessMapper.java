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
    @Mapping(target = "iban", source = "iban")
    @Mapping(target = "bankCode", expression = "java(iban != null ? org.iban4j.Iban.valueOf(iban).getBankCode() : null)")
    BankAccessEntity toBankAccessEntity(BankAccessTO bankAccessTO, String userId, boolean temporary, String iban);

}

