package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.adorsys.multibanking.domain.common.AbstractId;
import domain.BankApiUser;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class UserEntity extends AbstractId {
    private String id;
    private Date expireUser;

    private List<BankApiUser> apiUser = new ArrayList<>();

    public UserEntity id(String id) {
        this.id = id;
        return this;
    }
}
