package de.adorsys.multibanking.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 24.05.17.
 */
@Data
@Document
public class KeyStoreEntity {

    @Id
    private String id;
    private byte[] EncData;
}
