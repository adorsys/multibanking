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

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.PaymentStatus;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.PaymentStatusResponse;
import de.adorsys.multibanking.domain.transaction.PaymentStatusReqest;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVInstanstUebSEPAStatus;
import org.kapott.hbci.GV_Result.GVRInstantUebSEPAStatus;
import org.kapott.hbci.status.HBCIExecStatus;

@Slf4j
public class InstantPaymentStatusJob extends ScaAwareJob<PaymentStatusReqest, PaymentStatusResponse> {

    private GVInstanstUebSEPAStatus paymentStatusHbciJob;

    public InstantPaymentStatusJob(TransactionRequest<PaymentStatusReqest> transactionRequest, HbciBpdCacheHolder bpdCacheHolder) {
        super(transactionRequest, bpdCacheHolder);
    }

    public static PaymentStatus mapPaymentStatus(int hbciStatus) {
//        1: in Terminierung
//        2: abgelehnt von erster Inkassostelle
//        3: in Bearbeitung
//        4: Creditoren-seitig verarbeitet, Buchung veranlasst
//        5: R-Transaktion wurde veranlasst
//        6: Auftrag fehlgeschagen
//        7: Auftrag ausgeführt; Geld für den Zahlungsempfänger verfügbar
//        8: Abgelehnt durch Zahlungsdienstleister des Zahlers
//        9: Abgelehnt durch Zahlungsdienstleister des Zahlungsempfängers
        PaymentStatus paymentStatus = null;
        switch (hbciStatus) {
            case 1:
                paymentStatus = PaymentStatus.CANC;
                break;
            case 2:
                paymentStatus = PaymentStatus.RJCT;
                break;
            case 3:
                paymentStatus = PaymentStatus.ACTC;
                break;
            case 4:
                paymentStatus = PaymentStatus.ACSC;
                break;
            case 5:
                paymentStatus = PaymentStatus.CANC;
                break;
            case 6:
                paymentStatus = PaymentStatus.RJCT;
                break;
            case 7:
                paymentStatus = PaymentStatus.ACCC;
                break;
            case 8:
                paymentStatus = PaymentStatus.RJCT;
                break;
            case 9:
                paymentStatus = PaymentStatus.RJCT;
                break;
            default:
                log.warn("unexpected payment status: " + hbciStatus);
        }
        return paymentStatus;
    }

    @Override
    GVInstanstUebSEPAStatus createHbciJob() {
        paymentStatusHbciJob = new GVInstanstUebSEPAStatus(dialog.getPassport());
        paymentStatusHbciJob.setParam("my", getHbciKonto());
        paymentStatusHbciJob.setParam("orderid", transactionRequest.getTransaction().getPaymentId());
        return paymentStatusHbciJob;
    }

    @Override
    String getHbciJobName() {
        return GVInstanstUebSEPAStatus.getLowlevelName();
    }

    @Override
    void checkExecuteStatus(HBCIExecStatus execStatus) {
        //noop
    }

    @Override
    public PaymentStatusResponse createJobResponse() {
        GVRInstantUebSEPAStatus hbciStatus = (GVRInstantUebSEPAStatus) paymentStatusHbciJob.getJobResult();
        return new PaymentStatusResponse(mapPaymentStatus(hbciStatus.getStatus()));
    }
}
