package hbci4java;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.LogFilter;
import org.kapott.hbci.passport.HBCIPassportPinTanNoFile;
import org.kapott.hbci.security.Sig;

import java.io.IOException;
import java.util.*;

/**
 * Created by alexg on 08.02.17.
 */
public class HbciPassport extends HBCIPassportPinTanNoFile {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Optional<State> state = Optional.empty();

    //needed for jackson
    public HbciPassport() {
        super(null, null, null);
    }

    public HbciPassport(String passportState, Properties properties, HbciCallback hbciCallback, Object initObject) {
        super(properties, hbciCallback != null ? hbciCallback : new HbciCallback(), initObject);

        if (passportState != null) {
            state = Optional.of(HbciPassport.State.readJson(passportState));
            state.get().apply(this);
        }
    }

    public Optional<State> getState() {
        return state;
    }

    @Override
    public byte[] hash(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public byte[] sign(byte[] bytes) {
        try {
            // TODO: wenn die eingegebene PIN falsch war, muss die irgendwie
            // resettet werden, damit wieder danach gefragt wird
            if (getPIN() == null) {
                StringBuffer s = new StringBuffer();

                callback.callback(this,
                        HbciCallback.NEED_PT_PIN,
                        HBCIUtils.getLocMsg("CALLB_NEED_PTPIN"),
                        HbciCallback.TYPE_SECRET, s);
                if (s.length() == 0) {
                    throw new HBCI_Exception(
                            HBCIUtils.getLocMsg("EXCMSG_PINZERO"));
                }
                setPIN(s.toString());
                LogFilter.getInstance().addSecretData(getPIN(), "X",
                        LogFilter.FILTER_SECRETS);
            }

            String tan = "";

            // tan darf nur beim einschrittverfahren oder bei
            // PV=1 und passport.contains(challenge) und tan-pflichtiger auftrag
            // oder bei
            // PV=2 und passport.contains(challenge+reference) und HKTAN
            // ermittelt werden

            String pintanMethod = getCurrentTANMethod(false);

            if (pintanMethod.equals(Sig.SECFUNC_SIG_PT_1STEP)) {
                // nur beim normalen einschritt-verfahren muss anhand der
                // segment-
                // codes ermittelt werden, ob eine tan benötigt wird
                HBCIUtils.log("onestep method - checking GVs to decide whether or not we need a TAN",
                        HBCIUtils.LOG_DEBUG);

                // segment-codes durchlaufen
                String codes = collectSegCodes(new String(bytes, "ISO-8859-1"));
                StringTokenizer tok = new StringTokenizer(codes, "|");

                while (tok.hasMoreTokens()) {
                    String code = tok.nextToken();
                    String info = getPinTanInfo(code);

                    if (info.equals("J")) {
                        // für dieses segment wird eine tan benötigt
                        HBCIUtils.log("the job with the code " + code + " needs a TAN", HBCIUtils.LOG_DEBUG);

                        if (tan.length() == 0) {
                            // noch keine tan bekannt --> callback

                            StringBuffer callbackReturn = new StringBuffer();
                            getCallback().callback(
                                    this,
                                    HbciCallback.NEED_PT_TAN,
                                    HBCIUtils
                                            .getLocMsg("CALLB_NEED_PTTAN"),
                                    HbciCallback.TYPE_TEXT, callbackReturn);
                            if (callbackReturn.length() == 0) {
                                throw new HBCI_Exception(
                                        HBCIUtils.getLocMsg("EXCMSG_TANZERO"));
                            }
                            tan = callbackReturn.toString();
                        } else {
                            HBCIUtils.log("there should be only one job that needs a TAN!",
                                    HBCIUtils.LOG_WARN);
                        }

                    } else if (info.equals("N")) {
                        HBCIUtils.log("the job with the code " + code
                                + " does not need a TAN", HBCIUtils.LOG_DEBUG);

                    } else if (info.length() == 0) {
                        // TODO: ist das hier dann nicht ein A-Segment? In dem
                        // Fall
                        // wäre diese Warnung überflüssig
                        HBCIUtils.log("the job with the code " + code
                                        + " seems not to be allowed with PIN/TAN",
                                HBCIUtils.LOG_WARN);
                    }
                }
            } else {
                HBCIUtils
                        .log("twostep method - checking passport(challenge) to decide whether or not we need a TAN",
                                HBCIUtils.LOG_DEBUG);
                Properties secmechInfo = getCurrentSecMechInfo();

                // gespeicherte challenge aus passport holen
                String challenge = (String) getPersistentData("pintan_challenge");
                setPersistentData("pintan_challenge", null);

                if (challenge == null) {
                    // es gibt noch keine challenge
                    HBCIUtils
                            .log("will not sign with a TAN, because there is no challenge",
                                    HBCIUtils.LOG_DEBUG);
                } else {
                    HBCIUtils.log(
                            "found challenge in passport, so we ask for a TAN",
                            HBCIUtils.LOG_DEBUG);
                    // es gibt eine challenge, also damit tan ermitteln

                    StringBuffer s = new StringBuffer();
                    callback.callback(
                            this,
                            HbciCallback.NEED_PT_TAN,
                            secmechInfo.getProperty("name") + " "
                                    + secmechInfo.getProperty("inputinfo")
                                    + ": " + challenge, HbciCallback.TYPE_TEXT,
                            s);
                    if (s.length() == 0) {
                        throw new HBCI_Exception(
                                HBCIUtils.getLocMsg("EXCMSG_TANZERO"));
                    }
                    tan = s.toString();
                }
            }
            if (tan.length() != 0) {
                LogFilter.getInstance().addSecretData(tan, "X",
                        LogFilter.FILTER_SECRETS);
            }

            return (getPIN() + "|" + tan).getBytes("ISO-8859-1");
        } catch (Exception ex) {
            throw new HBCI_Exception("*** signing failed", ex);
        }
    }

    @Override
    public boolean verify(byte[] bytes, byte[] bytes1) {
        return true;
    }

    @Override
    public byte[][] encrypt(byte[] bytes) {
        try {
            int padLength = bytes[bytes.length - 1];
            byte[] encrypted = new String(bytes, 0, bytes.length
                    - padLength, "ISO-8859-1").getBytes("ISO-8859-1");
            return new byte[][]{new byte[8], encrypted};
        } catch (Exception ex) {
            throw new HBCI_Exception("*** encrypting message failed", ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] bytes, byte[] bytes1) {
        try {
            return new String(new String(bytes1, "ISO-8859-1") + '\001')
                    .getBytes("ISO-8859-1");
        } catch (Exception ex) {
            throw new HBCI_Exception("*** decrypting of message failed", ex);
        }
    }

    @Override
    public void resetPassphrase() {
    }

    @Override
    public void saveChanges() {
        state = Optional.of(new State(this, getState()));
    }

    public HbciPassport clone() {
        HbciPassport passport = new HbciPassport(null, getProperties(), null, null);
        passport.setCountry(this.getCountry());
        passport.setHost(this.getHost());
        passport.setPort(this.getPort());
        passport.setUserId(this.getUserId());
        passport.setSysId(this.getSysId());
        passport.setBPD(this.getBPD());
        passport.setUPD(this.getUPD());
        passport.setHBCIVersion(this.getHBCIVersion());
        passport.setCustomerId(this.getCustomerId());
        passport.setFilterType(this.getFilterType());
        passport.setAllowedTwostepMechanisms(this.getAllowedTwostepMechanisms());
        passport.setCurrentTANMethod(this.getCurrentTANMethod(false));
        passport.setPIN(this.getPIN());
        passport.setPersistentData(this.getPersistentData());

        passport.unsetComm();
        passport.setCallback(null);
        return passport;
    }

    /**
     * Captures the internal state of this passport. This mirrors what the {@link org.kapott.hbci.passport.HBCIPassportPinTan} writes in a file.
     * <p>
     * All fields are non-final public so that jackson can easily serialize them.
     */
    public static class State {
        public String country;
        public String host;
        public int port;
        public String userId;
        public String sysId;
        public Properties bpd;
        public Properties upd;
        public String hbciVersion;
        public String customerId;
        public String filterType;
        public List<String> allowedTwostepMechanisms;
        public String currentTANMethod;

        /**
         * Default constructor is needed by jackson
         */
        State() {
        }

        public State(HbciPassport passport) {
            this(passport, null);
        }

        /**
         * Creates a new State snapshot of the supplied passport. If oldState is non-null, its properties are used as fallback. This is useful so that the meta info of the UPD does not need to be refetched.
         */
        public State(HbciPassport passport, Optional<State> optionalOldState) {
            country = passport.getCountry();
            host = passport.getHost();
            port = passport.getPort();
            userId = passport.getUserId();
            sysId = passport.getSysId();
            hbciVersion = passport.getHBCIVersion();
            customerId = passport.getCustomerId();
            filterType = passport.getFilterType();
            allowedTwostepMechanisms = passport.getAllowedTwostepMechanisms();
            currentTANMethod = passport.getCurrentTANMethod(false);

            optionalOldState.ifPresent(oldState -> {
                bpd = mergeProperties(oldState.bpd, passport.getBPD());
                upd = mergeProperties(oldState.upd, passport.getUPD());
            });
        }

        private static Properties mergeProperties(Properties oldP, Properties newP) {
            if (oldP == null && newP == null) return null;
            Properties result = new Properties();
            if (oldP != null) result.putAll(oldP);
            if (newP != null) result.putAll(newP);
            return result;
        }

        public void apply(HbciPassport passport) {
            passport.setCountry(country);
            passport.setHost(host);
            passport.setPort(port);
            passport.setUserId(userId);
            passport.setSysId(sysId);
            passport.setBPD(bpd == null ? null : (Properties) bpd.clone());
            passport.setUPD(upd == null ? null : (Properties) upd.clone());
            passport.setHBCIVersion(hbciVersion);
            passport.setCustomerId(customerId);
            passport.setFilterType(filterType);
            passport.setAllowedTwostepMechanisms(new ArrayList<>(allowedTwostepMechanisms));
            passport.setCurrentTANMethod(currentTANMethod);
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
