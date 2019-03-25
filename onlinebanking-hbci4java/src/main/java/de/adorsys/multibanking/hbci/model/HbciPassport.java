/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    HbciPassport(String hbciversion, Map<String, String> properties, HbciCallback hbciCallback,
                 HBCIProduct hbciProduct) {
        super(hbciversion, properties, hbciCallback != null ? hbciCallback : new HbciCallback(), hbciProduct);
    }

    /**
     * Captures the internal state of this passport.
     * <p>
     * All fields are non-final public so that jackson can easily serialize them.
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class State {

        private HBCIProduct hbciProduct;
        private String hbciVersion;
        private String customerId;
        private String blz;
        private String userId;

        private String country;
        private String host;
        private int port;
        private String sysId;
        private HashMap<String, String> upd;

        private List<String> allowedTwostepMechanisms;
        private List<GVRTANMediaList.TANMediaInfo> tanMedias;
        private HBCITwoStepMechanism currentSecMechInfo;

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
            upd = (HashMap<String, String>) passport.getUPD();
            tanMedias = passport.getTanMedias();
            currentSecMechInfo = passport.getCurrentSecMechInfo();
            hbciProduct = passport.getHbciProduct();
        }

        public static State fromJson(String s) {
            try {
                return OBJECT_MAPPER.readValue(s, State.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public void apply(HbciPassport passport) {
            passport.setCountry(country);
            passport.setHost(host);
            passport.setPort(port);
            passport.setUserId(userId);
            passport.setSysId(sysId);
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
                throw new IllegalStateException(e);
            }
        }
    }
}
