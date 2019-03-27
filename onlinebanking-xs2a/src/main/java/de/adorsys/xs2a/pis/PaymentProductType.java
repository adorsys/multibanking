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

import de.adorsys.xs2a.error.XS2AClientException;

import java.util.Arrays;

public enum PaymentProductType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers"),
    TARGET_2_PAYMENTS("target-2-payments"),
    CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers"),
    PAIN_001_SEPA_CREDIT_TRANSFERS("pain.001-sepa-credit-transfers", true),
    PAIN_001_INSTANT_SEPA_CREDIT_TRANSFERS("pain.001-instant-sepa-credit-transfers", true),
    PAIN_001_TARGET_2_PAYMENTS("pain.001-target-2-payments", true),
    PAIN_001_CROSS_BORDER_CREDIT_TRANSFERS("pain.001-cross-border-credit-transfers", true);

    private String type;
    private boolean isRaw;

    PaymentProductType(String type, boolean isRaw) {
        this.type = type;
        this.isRaw = isRaw;
    }

    PaymentProductType(String type) {
        this(type, false);
    }

    public String getType() {
        return type;
    }

    public boolean isRaw() {
        return isRaw;
    }

    public static PaymentProductType resolve(String type) {
        return Arrays.stream(values())
                       .filter(p -> p.type.equalsIgnoreCase(type))
                       .findFirst()
                       .orElseThrow(() -> new XS2AClientException(type + " product type doesn't support by the system"));
    }
}
