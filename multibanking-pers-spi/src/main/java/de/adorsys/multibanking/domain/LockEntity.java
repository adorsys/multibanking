package de.adorsys.multibanking.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class LockEntity {

    @Id
    private String id;

    private String name;

    private String value;

    private Date expires;

}
