package hbci4java;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV_Result.GVRTANMediaList;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexg on 08.02.17.
 */
@Slf4j
public class HbciPassport extends PinTanPassport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public HbciPassport(String hbciversion, HashMap<String, String> properties, HbciCallback hbciCallback) {
        super(hbciversion, properties, hbciCallback != null ? hbciCallback : new HbciCallback());
    }

    public HbciPassport clone() {
        HbciPassport passport = new HbciPassport(this.getHBCIVersion(), getProperties(), null);
        passport.setCountry(this.getCountry());
        passport.setHost(this.getHost());
        passport.setPort(this.getPort());
        passport.setUserId(this.getUserId());
        passport.setSysId(this.getSysId());
        passport.setBPD(this.getBPD());
        passport.setUPD(this.getUPD());
        passport.setCustomerId(this.getCustomerId());
        passport.setAllowedTwostepMechanisms(this.getAllowedTwostepMechanisms());
        passport.setPIN(this.getPIN());
        passport.setPersistentData(this.getPersistentData());
        return passport;
    }

    /**
     * Captures the internal state of this passport.
     * <p>
     * All fields are non-final public so that jackson can easily serialize them.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class State {

        public String hbciVersion;
        public String customerId;
        public String blz;
        public String userId;

        public String country;
        public String host;
        public int port;
        public String sysId;
        public HashMap<String, String> bpd;
        public HashMap<String, String> upd;

        public List<String> allowedTwostepMechanisms;
        public List<GVRTANMediaList.TANMediaInfo> tanMedias;
        public HBCITwoStepMechanism currentSecMechInfo;

        /**
         * Default constructor is needed by jackson
         */
        State() {
        }

        /**
         * Creates a new State snapshot of the supplied passport. If oldState is non-null, its properties are used as fallback. This is useful so that the meta info of the UPD does not need to be refetched.
         */
        public State(PinTanPassport passport) {
            country = passport.getCountry();
            host = passport.getHost();
            port = passport.getPort();
            blz = passport.getBLZ();
            userId = passport.getUserId();
            sysId = passport.getSysId();
            hbciVersion = passport.getHBCIVersion();
            customerId = passport.getCustomerId();
            allowedTwostepMechanisms = passport.getAllowedTwostepMechanisms();
            bpd = passport.getBPD();
            upd = passport.getUPD();
            tanMedias = passport.getTanMedias();
            currentSecMechInfo = passport.getCurrentSecMechInfo();
        }

        public void apply(HbciPassport passport) {
            passport.setCountry(country);
            passport.setHost(host);
            passport.setPort(port);
            passport.setUserId(userId);
            passport.setSysId(sysId);
            passport.setBPD(bpd == null ? null : (HashMap<String, String>) bpd.clone());
            passport.setUPD(upd == null ? null : (HashMap<String, String>) upd.clone());
            passport.setCustomerId(customerId);
            passport.setAllowedTwostepMechanisms(new ArrayList<>(allowedTwostepMechanisms));
            passport.setTanMedias(tanMedias);
            passport.setCurrentSecMechInfo(currentSecMechInfo);
        }

        public String toJson() {
            try {
                return OBJECT_MAPPER.writeValueAsString(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static State readJson(String s) {
            try {
                return OBJECT_MAPPER.readValue(s, State.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
