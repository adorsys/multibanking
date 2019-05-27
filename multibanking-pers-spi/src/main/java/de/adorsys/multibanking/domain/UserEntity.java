package de.adorsys.multibanking.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserEntity {

    private String id;
    private LocalDateTime expireUser;
    private LocalDateTime rulesLastChangeDate;
    private List<BankApiUser> apiUser = new ArrayList<>();

    public UserEntity id(String id) {
        this.id = id;
        return this;
    }
}
