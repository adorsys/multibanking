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

package de.adorsys.xs2a.pis;

import de.adorsys.multibanking.xs2a.error.XS2AClientException;
import de.adorsys.xs2a.pis.sepa.SepaBulkPaymentInitiationBodyBuilder;
import de.adorsys.xs2a.pis.sepa.SepaPeriodicPaymentInitiationBodyBuilder;
import de.adorsys.xs2a.pis.sepa.SepaSinglePaymentInitiationBodyBuilder;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.xs2a.pis.PaymentProductType.SEPA_CREDIT_TRANSFERS;
import static de.adorsys.xs2a.pis.PaymentServiceType.*;

public class PaymentInitiationBuilderStrategyImpl implements PaymentInitiationBuilderStrategy {

    private static final String BUILDER_NOT_FOUND_ERROR_MESSAGE = "Can't find payment initiation builder for product %s and service %s";
    private Map<String, PaymentInitiationBodyBuilder> builders;
    private PaymentInitiationBodyBuilder rawPaymentBuilder = new PainPaymentInitiationBodyBuilder();

    public PaymentInitiationBuilderStrategyImpl() {
        builders = new HashMap<>(3);
        builders.put(buildKey(SEPA_CREDIT_TRANSFERS, SINGLE), new SepaSinglePaymentInitiationBodyBuilder());
        builders.put(buildKey(SEPA_CREDIT_TRANSFERS, BULK), new SepaBulkPaymentInitiationBodyBuilder());
        builders.put(buildKey(SEPA_CREDIT_TRANSFERS, PERIODIC), new SepaPeriodicPaymentInitiationBodyBuilder());
    }

    private String buildKey(PaymentProductType productType, PaymentServiceType serviceType) {
        return productType.getType() + serviceType.getType();
    }

    @Override
    public PaymentInitiationBodyBuilder resolve(PaymentProductType product, PaymentServiceType service) {
        if (product.isRaw()) {
            return rawPaymentBuilder;
        }
        String key = buildKey(product, service);

        if (!builders.containsKey(key)) {
            throw new XS2AClientException(String.format(BUILDER_NOT_FOUND_ERROR_MESSAGE, product, service));
        }

        return builders.get(key);
    }
}
