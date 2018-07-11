package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by alexg on 18.05.17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    private String logo;
    private String homepage;
    private String hotline;
    private String email;
    private String mandateReference;
    private Cycle interval;
    private boolean cancelled;
}
