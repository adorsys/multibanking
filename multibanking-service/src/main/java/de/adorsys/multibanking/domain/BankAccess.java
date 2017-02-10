package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"passportState", "pin"})
public class BankAccess {

    @Id
    private String id;
    @Indexed
    private String userId;
    private String bankLogin;
    private String bankCode;
    private String passportState;
    @Transient
    private String pin;
}
