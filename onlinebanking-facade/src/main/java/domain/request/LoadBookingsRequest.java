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

package domain.request;

import domain.BankAccess;
import domain.BankAccount;
import domain.BankApiUser;
import domain.request.AbstractHbciRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class LoadBookingsRequest extends AbstractHbciRequest {

    private BankApiUser bankApiUser;
    private BankAccount bankAccount;
    private BankAccess bankAccess;
    private String bankCode;
    private String pin;

    private boolean raw;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private boolean withBalance;
    private boolean withStandingOrders;
    private boolean withTanTransportTypes;
}
