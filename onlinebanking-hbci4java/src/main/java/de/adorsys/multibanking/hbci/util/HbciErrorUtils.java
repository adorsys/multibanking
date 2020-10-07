/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.util;

import de.adorsys.multibanking.domain.Message;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import lombok.experimental.UtilityClass;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.status.HBCIMsgStatus;
import org.kapott.hbci.status.HBCIRetVal;
import org.kapott.hbci.status.HBCIStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.domain.Message.Severity.ERROR;
import static de.adorsys.multibanking.domain.Message.Severity.WARNING;

@UtilityClass
public class HbciErrorUtils {

    public RuntimeException handleHbciException(HBCI_Exception e) {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof MultibankingException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() instanceof MultibankingException) {
            return (MultibankingException) processException.getCause();
        }

        return e;
    }

    public MultibankingException toMultibankingException(List<HBCIMsgStatus> msgStatusList) {
        return new MultibankingException(MultibankingError.HBCI_ERROR, -1, null, msgStatusListToMessages(msgStatusList));
    }

    public MultibankingException toMultibankingException(HBCIStatus hbciStatus) {
        return new MultibankingException(MultibankingError.HBCI_ERROR, -1, null, collectMessages(hbciStatus.getRetVals()));
    }

    public List<Message> msgStatusListToMessages(List<HBCIMsgStatus> msgStatusList) {
        return Optional.ofNullable(msgStatusList)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(HbciErrorUtils::msgStatusToMessages)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public List<Message> msgStatusToMessages(HBCIMsgStatus msgStatus) {
        return Optional.of(msgStatus)
            .map(status -> Stream.of(
                status.globStatus.getRetVals(),
                status.segStatus.getRetVals())
            )
            .orElseGet(Stream::empty)
            .map(HbciErrorUtils::collectMessages)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Message> collectMessages(List<HBCIRetVal> hbciReturnValues) {
        return Optional.ofNullable(hbciReturnValues)
            .map(list -> list.stream()
                .map(retVal -> {
                    Message.Severity severity = retVal.code.startsWith("9") ? ERROR : WARNING;
                    return new Message(retVal.code, severity, null, retVal.text, null);
                }))
            .orElse(Stream.empty())
            .collect(Collectors.toList());
    }
}
