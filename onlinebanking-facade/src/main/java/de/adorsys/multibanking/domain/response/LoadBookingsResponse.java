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

package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.Booking;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class LoadBookingsResponse extends AbstractResponse {

    //possible multiple CAMT files
    private List<String> rawData;
    private List<Booking> bookings;
    private BalancesReport balancesReport;
    private List<BankAccount> bankAccounts;

}
