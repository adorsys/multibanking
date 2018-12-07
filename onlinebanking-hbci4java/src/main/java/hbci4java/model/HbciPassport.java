/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbci4java.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV_Result.GVRTANMediaList;
import org.kapott.hbci.manager.HBCIProduct;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexg on 08.02.17.
 */
@Slf4j
public class HbciPassport extends PinTanPassport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public HbciPassport(String hbciversion, Map<String, String> properties, HbciCallback hbciCallback, HBCIProduct hbciProduct) {
        super(hbciversion, properties, hbciCallback != null ? hbciCallback : new HbciCallback(), hbciProduct);
    }

    public HbciPassport clone() {
        HbciPassport passport = new HbciPassport(this.getHBCIVersion(), getProperties(), null, this.getHbciProduct());
        passport.setCountry(this.getCountry());
        passport.setHost(this.getHost());
        passport.setPort(this.getPort());
        passport.setUserId(this.getUserId());
        passport.setSysId(this.getSysId());
        passport.setBPD(this.getBPD());
        passport.setUPD(this.getUPD());
        passport.setCustomerId(this.getCustomerId());
        passport.setUserTwostepMechanisms(this.getUserTwostepMechanisms());
        passport.setPIN(this.getPIN());
        return passport;
    }

    /**
     * Captures the internal state of this passport.
     * <p>
     * All fields are non-final public so that jackson can easily serialize them.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class State {

        public HBCIProduct hbciProduct;
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
         * Creates a new State snapshot of the supplied passport. If oldState is non-null, its properties are used as
         * fallback. This is useful so that the meta info of the UPD does not need to be refetched.
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
            allowedTwostepMechanisms = passport.getUserTwostepMechanisms();
            bpd = (HashMap<String, String>) passport.getBPD();
            upd = (HashMap<String, String>) passport.getUPD();
            tanMedias = passport.getTanMedias();
            currentSecMechInfo = passport.getCurrentSecMechInfo();
            hbciProduct = passport.getHbciProduct();
        }

        public static State readJson(String s) {
            try {
                return OBJECT_MAPPER.readValue(s, State.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void apply(HbciPassport passport) {
            passport.setCountry(country);
            passport.setHost(host);
            passport.setPort(port);
            passport.setUserId(userId);
            passport.setSysId(sysId);
            passport.setBPD(bpd == null ? null : (Map<String, String>) bpd.clone());
            passport.setUPD(upd == null ? null : (Map<String, String>) upd.clone());
            passport.setCustomerId(customerId);
            passport.setUserTwostepMechanisms(new ArrayList<>(allowedTwostepMechanisms));
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
    }
}
