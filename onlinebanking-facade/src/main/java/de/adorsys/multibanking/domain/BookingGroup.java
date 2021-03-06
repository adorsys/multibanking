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

package de.adorsys.multibanking.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by alexg on 09.08.17.
 */
@Data
@Builder
public class BookingGroup {

    private Type type;
    private String name;
    private boolean salaryWage;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String otherAccount;
    private BigDecimal amount;
    private List<BookingPeriod> bookingPeriods;
    private Contract contract;

    public enum Type {
        STANDING_ORDER, RECURRENT_INCOME, RECURRENT_SEPA, RECURRENT_NONSEPA, CUSTOM, OTHER_INCOME, OTHER_EXPENSES
    }
}
