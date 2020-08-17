/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.VeuListResponse;
import de.adorsys.multibanking.domain.transaction.LoadVeuList;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVVeuList;

@Slf4j
public class VeuListJob extends ScaAwareJob<LoadVeuList, GVVeuList, VeuListResponse> {

    public VeuListJob(TransactionRequest<LoadVeuList> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    GVVeuList createHbciJob() {
        GVVeuList veuListJob = new GVVeuList(dialog.getPassport());
        veuListJob.setParam("my", getHbciKonto());
        return veuListJob;
    }

    @Override
    String getHbciJobName() {
        return GVVeuList.getLowlevelName();
    }

    @Override
    VeuListResponse createJobResponse() {
        return new VeuListResponse();
    }
}
