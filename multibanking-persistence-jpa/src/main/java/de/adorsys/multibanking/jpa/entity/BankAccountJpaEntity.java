package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApi;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.EnumMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@Entity(name = "bank_account")
@Data
public class BankAccountJpaEntity extends BankAccountCommonJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String bankAccessId;
    private String userId;
    private String psd2ConsentId;
    private String psd2ConsentAuthorisationId;

    @ElementCollection
    @CollectionTable(name = "bankaccount_external_id")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "bank_api")
    @Column(name = "external_id")
    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
    @Embedded
    private BalancesReportEntity balances;

}
