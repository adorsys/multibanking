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
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity(name = "booking_category")
@Data
@EqualsAndHashCode(callSuper = true)
public class BookingCategoryJpaEntity extends ContractJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    @ElementCollection
    @CollectionTable(
            name = "booking_categoryrule",
            joinColumns = @JoinColumn(name = "booking_id")
    )
    private List<String> rules;
    private String receiver;
    @ElementCollection
    @CollectionTable(name = "bookingcategory_custom")
    @MapKeyColumn(name = "bookingcategory_id")
    private Map<String, String> custom;

}
