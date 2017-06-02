package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import domain.BankApi;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
public class BankInfoEntity {

    @Id
    private String bankCode;

    private BankApi bankApi;

}
