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

package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ConsentStatus;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.TanTransportType;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.PaymentStatusReqest;
import de.adorsys.multibanking.hbci.job.InstantPaymentStatusJob;
import de.adorsys.multibanking.hbci.model.*;
import de.adorsys.multibanking.hbci.util.HbciErrorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.iban4j.Iban;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.GV_Result.GVRTANMediaList;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.dialog.HBCIUpdDialog;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIProduct;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;
import static de.adorsys.multibanking.domain.ScaStatus.*;
import static de.adorsys.multibanking.domain.exception.MultibankingError.BANK_NOT_SUPPORTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_SCA_METHOD;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.BPD;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.UPD;

@Slf4j
@RequiredArgsConstructor
public class HbciScaHandler implements StrongCustomerAuthorisable {

    private final HBCIProduct hbciProduct;
    private final long sysIdMaxAgeMs;
    private final long updMaxAgeMs;
    private final HbciBpdCacheHolder hbciBpdCacheHolder;

    private final HbciScaMapper hbciScaMapper = new HbciScaMapperImpl();
    private final HbciDialogRequestMapper hbciDialogRequestMapper = new HbciDialogRequestMapperImpl();

    @Override
    public CreateConsentResponse createConsent(Consent consent, boolean redirectPreferred,
                                               String tppRedirectUri, Object bankApiConsentData) {
        String bankCode = Iban.valueOf(consent.getPsuAccountIban()).getBankCode();

        boolean bankSupported = Optional.ofNullable(HBCIUtils.getBankInfo(bankCode))
            .map(bankInfo -> bankInfo.getPinTanVersion() != null)
            .orElse(false);

        if (!bankSupported) {
            log.error("Bank not supported for bank code " + bankCode);
            throw new MultibankingException(BANK_NOT_SUPPORTED, 400, "Bank code " + bankCode + " not supported");
        }

        HbciConsent hbciConsent = new HbciConsent();
        hbciConsent.setStatus(STARTED);
        hbciConsent.setHbciProduct(hbciProduct);

        return hbciScaMapper.toCreateConsentResponse(hbciConsent);
    }

    @Override
    public Consent getConsent(String consentId, Object bankApiConsentData) {
        return null;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId, Object bankApiConsentData) {
        return null;
    }

    @Override
    public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
        //credentials needed in consent for further requests
        HbciConsent hbciConsent = (HbciConsent) updatePsuAuthentication.getBankApiConsentData();
        hbciConsent.setCredentials(updatePsuAuthentication.getCredentials());

        ScaMethodsResponse response = authenticatePsu(updatePsuAuthentication);

        hbciConsent.setTanMethodList(response.getTanTransportTypes());
        hbciConsent.setStatus(PSUAUTHENTICATED);

        return hbciScaMapper.toUpdateAuthResponse(hbciConsent, new UpdateAuthResponse(HBCI, EMBEDDED,
            PSUAUTHENTICATED));
    }

    private ScaMethodsResponse authenticatePsu(UpdatePsuAuthenticationRequest authenticatePsuRequest) {
        try {
            HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(authenticatePsuRequest.getBank(), hbciBpdCacheHolder);
            HbciDialogRequest dialogRequest = hbciDialogRequestMapper.toHbciDialogRequest(authenticatePsuRequest, hbciCallback);

            HbciConsent hbciConsent = (HbciConsent) authenticatePsuRequest.getBankApiConsentData();
            hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

            AbstractHbciDialog dialog = HbciDialogFactory.createDialog(BPD, dialogRequest, null, hbciBpdCacheHolder.getBpd(dialogRequest));
            HBCIExecStatus bpdExecStatus = dialog.execute(true);
            boolean withHktan = !bpdExecStatus.hasMessage("9400");
            if (!withHktan) {
                hbciConsent.setWithHktan(false);
            }

            PinTanPassport passport = fetchUpd(dialogRequest, withHktan, dialog.getPassport().getBPD());

            List<GVRTANMediaList.TANMediaInfo> tanMediaList = null;
            if (passport.jobSupported(GVTANMediaList.getLowlevelName())) {
                tanMediaList = fetchTanMedias(passport);
            }

            ScaMethodsResponse response = ScaMethodsResponse.builder()
                .tanTransportTypes(extractTanTransportTypes(passport, tanMediaList))
                .build();
            response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));
            return response;
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisationRequest) {
        HbciConsent hbciConsent = (HbciConsent) transactionAuthorisationRequest.getBankApiConsentData();
        hbciConsent.setScaAuthenticationData(transactionAuthorisationRequest.getScaAuthenticationData());

        return hbciScaMapper.toUpdateAuthResponse(hbciConsent, new UpdateAuthResponse(HBCI, EMBEDDED,
            hbciConsent.getStatus()));
    }

    @Override
    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
        HbciConsent hbciConsent = (HbciConsent) selectPsuAuthenticationMethod.getBankApiConsentData();

        TanTransportType selectedMethod = hbciConsent.getTanMethodList().stream()
            .filter(tanTransportType -> tanTransportType.getId().equals(selectPsuAuthenticationMethod.getAuthenticationMethodId()))
            .filter(tanTransportType -> selectPsuAuthenticationMethod.getTanMediaName() == null
                || selectPsuAuthenticationMethod.getTanMediaName().equals(tanTransportType.getMedium()))
            .findFirst()
            .orElseThrow(() -> new MultibankingException(INVALID_SCA_METHOD));

        hbciConsent.setSelectedMethod(selectedMethod);
        hbciConsent.setStatus(SCAMETHODSELECTED);

        return hbciScaMapper.toUpdateAuthResponse(hbciConsent, new UpdateAuthResponse(HBCI, EMBEDDED,
            SCAMETHODSELECTED));
    }

    @Override
    public void revokeConsent(String consentId, Object bankApiConsentData) {
        //noop
    }

    @Override
    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                     Object bankApiConsentData) {
        HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;
        return hbciScaMapper.toUpdateAuthResponse(hbciConsent, new UpdateAuthResponse(HBCI, EMBEDDED,
            hbciConsent.getStatus()));
    }

    @Override
    public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                Object bankApiConsentData) {
        HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;

        if (hbciConsent.getHbciTanSubmit() != null && hbciConsent.getScaAuthenticationData() == null) {
            throw new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS);
        }

        if (hbciConsent.getStatus() == SCAMETHODSELECTED || hbciConsent.getStatus() == FINALISED) {
            return;
        }
        throw new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS);
    }

    @Override
    public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
        //tansubmit persistence fur further call
        HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;
        hbciConsent.setHbciTanSubmit(authorisationCodeResponse.getTanSubmit());
    }

    @Override
    public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(TransactionRequest<PaymentStatusReqest> request) {
        try {
            HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
            hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

            HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(request.getBank(), hbciBpdCacheHolder);

            InstantPaymentStatusJob instantPaymentStatusJob = new InstantPaymentStatusJob(request, hbciBpdCacheHolder);
            PaymentStatusResponse response = instantPaymentStatusJob.execute(hbciCallback);
            response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));

            return response;
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    private PinTanPassport fetchUpd(HbciDialogRequest dialogRequest, boolean withHktan, Map<String, String> bpd) {
        HBCIUpdDialog dialog = (HBCIUpdDialog) HbciDialogFactory.createDialog(UPD, dialogRequest, null, bpd);
        dialog.setWithHktan(withHktan);
        dialog.execute(true);

        return dialog.getPassport();
    }

    private List<GVRTANMediaList.TANMediaInfo> fetchTanMedias(PinTanPassport passport) {
        GVTANMediaList gvtanMediaList = new GVTANMediaList(passport);

        HBCIJobsDialog dialog = new HBCIJobsDialog(passport);
        dialog.dialogInit(true, "HKTAB");
        dialog.addTask(gvtanMediaList);
        dialog.execute(true);

        return ((GVRTANMediaList) gvtanMediaList.getJobResult()).mediaList();
    }

    private List<TanTransportType> extractTanTransportTypes(PinTanPassport hbciPassport,
                                                            List<GVRTANMediaList.TANMediaInfo> tanMediaList) {
        return hbciPassport.getUserTwostepMechanisms()
            .stream()
            .map(id -> hbciPassport.getBankTwostepMechanisms().get(id))
            .filter(Objects::nonNull)
            .map(hbciTwoStepMechanism -> TanTransportType.builder()
                .id(hbciTwoStepMechanism.getSecfunc())
                .name(hbciTwoStepMechanism.getName())
                .inputInfo(hbciTwoStepMechanism.getInputinfo())
                .needTanMedia(hbciTwoStepMechanism.getNeedtanmedia().equals("2"))
                .timeoutDecoupledFirstStatusRequest(hbciTwoStepMechanism.getTimeoutDecoupledFirstStatusRequest())
                .timeoutDecoupledNextStatusRequest(hbciTwoStepMechanism.getTimeoutDecoupledNextStatusRequest())
                .decoupledMaxStatusRequests(hbciTwoStepMechanism.getMaxDecoupledStatusRequests())
                .build())
            .map(tanTransportType -> {
                if (!tanTransportType.isNeedTanMedia()) {
                    return Collections.singletonList(tanTransportType);
                } else {
                    return tanMediaList.stream()
                        .map(tanMediaInfo -> {
                            TanTransportType clone = SerializationUtils.clone(tanTransportType);
                            clone.setMedium(tanMediaInfo.mediaName);
                            return clone;
                        })
                        .collect(Collectors.toList());
                }
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
