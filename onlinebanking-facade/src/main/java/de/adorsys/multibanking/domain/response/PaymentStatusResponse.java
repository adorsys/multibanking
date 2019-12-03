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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class PaymentStatusResponse extends AbstractResponse {

//    1: in Terminierung
//2: abgelehnt von erster Inkassostelle
//3: in Bearbeitung
//4: Creditoren-seitig verarbeitet, Buchung veranlasst
//5: R-Transaktion wurde veranlasst
//6: Auftrag fehlgeschagen
//7: Auftrag ausgeführt; Geld für den Zahlungsempfänger verfügbar
//8: Abgelehnt durch Zahlungsdienstleister des Zahlers
//9: Abgelehnt durch Zahlungsdienstleister des Zahlungsempfängers

}
