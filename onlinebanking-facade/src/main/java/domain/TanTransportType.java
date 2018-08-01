package domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by alexg on 11.09.17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TanTransportType {

    private String id;
    private String name;
    private String medium;
    private String inputInfo;
}
