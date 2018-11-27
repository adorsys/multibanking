package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankApiUser;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "expireUser"})
public class UserEntity {

    @Id
    private String id;

    @Indexed
    private LocalDateTime expireUser;

    private LocalDateTime rulesLastChangeDate;

    private List<BankApiUser> apiUser = new ArrayList<>();

    public UserEntity id(String id) {
        this.id = id;
        return this;
    }
}
