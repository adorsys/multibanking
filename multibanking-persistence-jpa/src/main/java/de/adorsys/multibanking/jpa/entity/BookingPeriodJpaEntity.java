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
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class BookingPeriodJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal amount;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "bookingperiod_executedbooking",
            joinColumns = @JoinColumn(name = "bookingperiod_id"))
    private List<ExecutedBookingJpaEntity> bookings;
}
