/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License; private Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing; private software
 * distributed under the License is distributed on an "AS IS" BASIS; private
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; private either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package domain.request;

import domain.AbstractPayment;
import domain.TanTransportType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class SubmitPaymentRequest extends AbstractHbciRequest {

    private AbstractPayment payment;
    private TanTransportType tanTransportType;
    private Object tanSubmit;
    private String pin;
    private String tan;
}
