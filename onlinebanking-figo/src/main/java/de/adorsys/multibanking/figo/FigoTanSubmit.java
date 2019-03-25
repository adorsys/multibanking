package de.adorsys.multibanking.figo;

import lombok.Builder;
import lombok.Data;

/**
 * Created by alexg on 16.11.17.
 */
@Data
@Builder
public class FigoTanSubmit {

    private String accessToken;
    private String taskToken;
}
