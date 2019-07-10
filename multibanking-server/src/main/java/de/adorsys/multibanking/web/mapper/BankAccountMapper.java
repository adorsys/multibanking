package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.web.model.BankAccountTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    BankAccountTO toBankAccountTO(BankAccountEntity bankAccountEntity);

    List<BankAccountTO> toBankAccountTOs(List<BankAccountEntity> bankAccountEntities);

}
