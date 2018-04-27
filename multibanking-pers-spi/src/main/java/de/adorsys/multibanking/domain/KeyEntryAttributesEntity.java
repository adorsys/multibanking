package de.adorsys.multibanking.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class KeyEntryAttributesEntity {

    public enum KeyUsage {
        Encryption,
        Signature,
        SecretKey;
    }

    public enum State {
        CREATED,
        VALID,
        LEGACY,
        EXPIRED;
    }

    private String alias;

    private Date createdAt;
    private Date notBefore;
    private Date notAfter;
    private Date expireAt;

    private Long validityInterval;
    private Long legacyInterval;

    private State state;

    private KeyUsage keyUsage;
}
