package de.adorsys.multibanking.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
public class UserEntity {

    @Id
    private String id;

    public UserEntity id(String id) {
        this.id = id;
        return this;
    }
}
