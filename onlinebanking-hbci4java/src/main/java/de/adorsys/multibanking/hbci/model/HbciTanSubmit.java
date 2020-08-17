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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;

import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;

@Slf4j
@Data
public class HbciTanSubmit {

    private String dialogId;
    private long msgNum;
    private String orderRef;
    private String passportState;
    private String hbciJobName; //eg. HKCCS
    private String originJobName; //"org.kapott.hbci.GV.GV" + jobname
    private String originLowLevelName; //key for hbci-300.xml
    private String hktanProcess; //1,2,3,4,S
    private int originSegVersion; //segment version
    private String sepaPain;
    private String painVersion;
    private HBCITwoStepMechanism twoStepMechanism;
    private String lowLevelParams;
    private boolean veu;

    public void update(AbstractHbciDialog dialog, AbstractHBCIJob hbciJob, String originJobName,
                       HBCITwoStepMechanism twoStepMechanism, String accountNumber) {
        setOriginJobName(originJobName);
        setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        setDialogId(dialog.getDialogId());
        setMsgNum(dialog.getMsgnum());
        setTwoStepMechanism(twoStepMechanism);

        if (hbciJob != null) {
            try {
                setLowLevelParams(new ObjectMapper().writeValueAsString(hbciJob.getLowlevelParams()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new MultibankingException(INTERNAL_ERROR, 500, e.getMessage());
            }

            Optional.ofNullable(hbciJob.getPainVersion())
                .ifPresent(version -> setPainVersion(version.getURN()));
            setOriginLowLevelName(hbciJob.getJobName());
            setOriginSegVersion(hbciJob.getSegVersion());
            setHbciJobName(hbciJob.getHBCICode());
            setVeu(dialog.getPassport().getRequiredSigsCount(accountNumber, hbciJob.getHBCICode()) > 1);
        }
    }

}
