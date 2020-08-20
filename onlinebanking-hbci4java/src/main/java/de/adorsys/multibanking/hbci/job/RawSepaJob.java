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

import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.transaction.RawSepaPayment;
import org.apache.commons.lang3.math.NumberUtils;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV.GVInstantUebSEPA;
import org.kapott.hbci.GV.GVRawSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.sepa.SepaVersion;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RawSepaJob extends AbstractPaymentJob<RawSepaPayment, GVRawSEPA> {

    public RawSepaJob(TransactionRequest<RawSepaPayment> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    String getHbciJobName() {
        return GVRawSEPA.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    GVRawSEPA createHbciJob() {
        RawSepaPayment sepaPayment = transactionRequest.getTransaction();

        String jobName;
        switch (sepaPayment.getSepaTransactionType()) {
            case SINGLE_PAYMENT:
                jobName = GVUebSEPA.getLowlevelName();
                break;
            case INSTANT_PAYMENT:
                jobName = GVInstantUebSEPA.getLowlevelName();
                break;
            case BULK_PAYMENT:
                jobName = "SammelUebSEPA";
                break;
            case STANDING_ORDER:
                jobName = GVDauerSEPANew.getLowlevelName();
                break;
            default:
                throw new IllegalArgumentException("unsupported raw sepa transaction: " + sepaPayment.getSepaTransactionType());
        }

        return createRawSepaJob(sepaPayment, jobName);
    }

    private GVRawSEPA createRawSepaJob(RawSepaPayment sepaPayment, String jobName) {
        GVRawSEPA rawSEPAJob = new GVRawSEPA(dialog.getPassport(), jobName, sepaPayment.getRawRequestData());
        rawSEPAJob.setParam("src", getHbciKonto());

        String creditorIban = "";
        BigDecimal amount = new BigDecimal(0);
        String currency = "";

        List<Map<String, String>> result = parsePain(sepaPayment.getPainXml());
        for (Map<String, String> resultMap : result) {
            creditorIban = resultMap.get("dst.iban");
            amount = amount.add(NumberUtils.createBigDecimal(resultMap.get("value")));

            String tempCurrency = resultMap.get("curr");
            if (currency.length() > 0 && !currency.equals(tempCurrency)) {
                throw new IllegalArgumentException("mixed currencies in bulk payment");
            }
            currency = tempCurrency;
        }

        if (result.size() > 1) {
            rawSEPAJob.setLowlevelParam(rawSEPAJob.getName() + ".sepa.dst.iban", creditorIban);
        }

        rawSEPAJob.setLowlevelParam(rawSEPAJob.getName() + ".sepa.btg.value", amount.toString());
        rawSEPAJob.setLowlevelParam(rawSEPAJob.getName() + ".sepa.btg.curr", currency);
        return rawSEPAJob;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parsePain(String painXml) {
        List<Map<String, String>> sepaResults = new ArrayList<>();
        ISEPAParser<List<Map<String, String>>> parser =
            SEPAParserFactory.get(SepaVersion.autodetect(painXml));
        parser.parse(new ByteArrayInputStream(painXml.getBytes(StandardCharsets.UTF_8)), sepaResults);
        return sepaResults;
    }
}
