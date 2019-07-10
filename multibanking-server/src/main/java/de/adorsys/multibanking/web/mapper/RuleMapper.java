package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.web.model.RuleTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RuleMapper {

    RuleTO toRuleTO(RuleEntity ruleEntity);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "searchIndex", ignore = true)
    RuleEntity toRuleEntity(RuleTO ruleEntity);

    List<RuleTO> toRuleTOs(List<RuleEntity> ruleEntities);

}
