package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document
@Encrypted(exclude = {"_id", "expireUser"})
public class UserMongoEntity {

    @Id
    private String id;

    @Indexed
    private LocalDateTime expireUser;

    private LocalDateTime rulesLastChangeDate;

    private List<BankApiUser> apiUser = new ArrayList<>();

    public UserMongoEntity id(String id) {
        this.id = id;
        return this;
    }
}
