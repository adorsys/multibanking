package domain;

import lombok.Builder;
import lombok.Data;

/**
 * Created by alexg on 18.05.17.
 */
@Data
@Builder
public class Contract {

    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandateReference;
    private Cycle interval;
}
