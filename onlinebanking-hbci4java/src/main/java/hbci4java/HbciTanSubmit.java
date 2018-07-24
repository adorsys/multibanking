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

    private String hbciPassport;
    private String dialogId;
    private long msgNum;
    private HbciGVTanSubmit gvTanSubmit;
}
