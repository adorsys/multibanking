package hbci4java;

import lombok.Builder;
import lombok.Data;
import org.kapott.hbci.GV.GVTAN2Step;

import java.util.Properties;

/**
 * Created by alexg on 16.11.17.
 */
@Data
public class HbciTanSubmit {

    private String dialogId;
    private long msgNum;
    private String orderRef;
    private String passportState;
    private String originJobName;
    private int originSegVersion;
    private String sepaPain;
}
