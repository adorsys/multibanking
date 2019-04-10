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

package de.adorsys.multibanking.bg.pis;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;

import java.util.Arrays;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PAYMENT;

public enum PaymentServiceType {
    SINGLE("payments", SinglePayment.class),
    BULK("bulk-payments", BulkPayment.class),
    PERIODIC("periodic-payments", FutureSinglePayment.class);

    private String type;
    private Class<? extends AbstractScaTransaction> clazz;

    PaymentServiceType(String type, Class<? extends AbstractScaTransaction> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public static PaymentServiceType resolve(String service) {
        return Arrays.stream(values())
                .filter(s -> s.type.equalsIgnoreCase(service))
                .findFirst()
                .orElseThrow(() -> new MultibankingException(INVALID_PAYMENT, service + " service type not supported " +
                        "by the system"));
    }

    public static <P extends AbstractScaTransaction> PaymentServiceType resolve(P payment) {
        if (payment instanceof RawSepaPayment) {
            if (payment.getRawData() == null) {
                throw new MultibankingException(INVALID_PAYMENT, "Payment body is absent");
            }
            return resolve(((RawSepaPayment) payment).getService());
        }
        return Arrays.stream(values())
                .filter(s -> s.clazz == payment.getClass())
                .findFirst()
                .orElseThrow(() -> new MultibankingException(INVALID_PAYMENT, payment.getClass().getName() + " " +
                        "service class not " +
                        "supported by the system"));
    }

    public String getType() {
        return type;
    }

    public Class<? extends AbstractScaTransaction> getClazz() {
        return clazz;
    }
}
