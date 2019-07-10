package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.web.model.BankTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankMapper {

    BankTO toBankTO(BankEntity bankEntity);
}
