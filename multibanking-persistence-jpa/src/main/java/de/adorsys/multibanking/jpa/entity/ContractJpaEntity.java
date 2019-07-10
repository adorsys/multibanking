package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.Cycle;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity(name = "contract")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    private String accountId;

    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandateReference;
    @Column(name = "cycle")
    private Cycle interval;
    private boolean cancelled;

    private BigDecimal amount;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String provider;

}
