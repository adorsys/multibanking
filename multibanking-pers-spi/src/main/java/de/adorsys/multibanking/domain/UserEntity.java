package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankApiUser;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@Encrypted(exclude = "_id")
public class UserEntity {

    @Id
    private String id;

    private List<BankApiUser> apiUser;

    public UserEntity id(String id) {
        this.id = id;
        return this;
    }
}
