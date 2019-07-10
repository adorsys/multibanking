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

package de.adorsys.multibanking.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class BookingGroupJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private Type type;
    private String name;
    private boolean salaryWage;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String otherAccount;
    private BigDecimal amount;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "bookinggroup_period",
            joinColumns = @JoinColumn(name = "bookinggroup_id"))
    private List<BookingPeriodJpaEntity> bookingPeriods;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_id")
    private ContractJpaEntity contract;

    public enum Type {
        STANDING_ORDER, RECURRENT_INCOME, RECURRENT_SEPA, RECURRENT_NONSEPA, CUSTOM, OTHER_INCOME, OTHER_EXPENSES
    }
}
