package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApiUser;
import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name="mbs_user")
@Data
public class UserJpaEntity {

    @Id
    //@GeneratedValue
    private Long id;
    private LocalDateTime expireUser;
    private LocalDateTime rulesLastChangeDate;
    @Embedded
    private List<BankApiUser> apiUser = new ArrayList<>();

}
