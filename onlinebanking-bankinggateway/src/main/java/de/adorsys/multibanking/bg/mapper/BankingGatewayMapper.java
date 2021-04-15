package de.adorsys.multibanking.bg.mapper;

import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.Message;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.xs2a_adapter.model.AccountDetails;
import de.adorsys.multibanking.xs2a_adapter.model.TppMessage400AIS;
import de.adorsys.multibanking.xs2a_adapter.model.TransactionDetails;
import org.apache.commons.codec.binary.Base64;
import org.iban4j.Iban;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.*;

import static de.adorsys.multibanking.domain.BankAccountType.fromXS2AType;
import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static java.nio.charset.StandardCharsets.UTF_8;

@Mapper
public interface BankingGatewayMapper {

    @Mapping(source = "psuAccountIban", target = "psuAccount")
    @Mapping(source = "accounts", target = "access.accounts")
    @Mapping(source = "balances", target = "access.balances")
    @Mapping(source = "transactions", target = "access.transactions")
    @Mapping(target = "consentStatus", ignore = true)
    @Mapping(target = "psuCorporateId", ignore = true)
    ConsentTO toConsentTO(Consent consentTemplate);

    @Mapping(target = "redirectId", ignore = true)
    @Mapping(target = "temporary", ignore = true)
    @InheritInverseConfiguration
    Consent toConsent(ConsentTO consentTO);

    @Mapping(target = "authorisationCodeResponse", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "bankApiConsentData", ignore = true)
    @Mapping(target = "redirectId", ignore = true)
    CreateConsentResponse toCreateConsentResponse(CreateConsentResponseTO consentResponse);

    @Mapping(target = "psuId", source = "customerId")
    @Mapping(target = "psuCorporateId", source = "userId")
    @Mapping(target = "password", source = "pin")
    UpdatePsuAuthenticationRequestTO toUpdatePsuAuthenticationRequestTO(Credentials credentials);

    @Mapping(target = "authorisationCodeResponse", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "bankApiConsentData", ignore = true)
    UpdateAuthResponse toUpdateAuthResponse(ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO,
                                            @MappingTarget UpdateAuthResponse updateAuthResponse);

    SelectPsuAuthenticationMethodRequestTO toSelectPsuAuthenticationMethodRequestTO(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    TransactionAuthorisationRequestTO toTransactionAuthorisationRequestTO(TransactionAuthorisationRequest transactionAuthorisation);

    @Mapping(target = "id", source = "authenticationMethodId")
    @Mapping(target = "inputInfo", source = "explanation")
    @Mapping(target = "medium", source = "name")
    @Mapping(target = "type", source = "authenticationType")
    @Mapping(target = "needTanMedia", ignore = true)
    TanTransportType toTanTransportType(ScaMethodTO scaMethodTO);

    List<BankAccount> toBankAccounts(List<AccountDetails> accountDetails);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "owner", source = "ownerName")
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSync", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "bic", ignore = true)
    @Mapping(target = "externalIdMap", expression = "java(getExternalIdMap(accountDetails.getResourceId()))")
    @Mapping(target = "blz", expression = "java(getBlz(accountDetails))")
    @Mapping(target = "accountNumber", expression = "java(getAccountNumber(accountDetails))")
    @Mapping(target = "type", expression = "java(getAccounType(accountDetails.getCashAccountType()))")
    BankAccount toBankAccount(AccountDetails accountDetails);

    default Map<BankApi, String> getExternalIdMap(String accountResourceId) {
        Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
        externalIdMap.put(XS2A, accountResourceId);
        return externalIdMap;
    }

    default String getBlz(AccountDetails accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getBankCode();
    }

    default String getAccountNumber(AccountDetails accountDetails) {
        return Iban.valueOf(accountDetails.getIban()).getAccountNumber();
    }

    default BankAccountType getAccounType(String cashAccountType) {
        return fromXS2AType(cashAccountType);
    }

    default String byteToString(byte[] value) {
        return new String(value, UTF_8);
    }

    default Booking toBooking(TransactionDetails transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(XS2A);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        if (transactionDetails.getTransactionAmount() != null) {
            booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
            booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        }
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());
        booking.setTransactionCode(transactionDetails.getPurposeCode() == null ? null :
            transactionDetails.getPurposeCode().toString());
        booking.setProprietaryBankTransactionCode(transactionDetails.getProprietaryBankTransactionCode());

        // balance after transaction
        Optional.ofNullable(transactionDetails.getBalanceAfterTransaction())
            .map(de.adorsys.multibanking.xs2a_adapter.model.Balance::getBalanceAmount)
            .map(de.adorsys.multibanking.xs2a_adapter.model.Amount::getAmount)
            .map(BigDecimal::new)
            .ifPresent(booking::setBalance);

        if(transactionDetails.getAdditionalInformation() != null) {
            booking.setText(transactionDetails.getAdditionalInformation());
        } else {
            // fallback use gvcode from bank transaction code to lookup buchungstext
            String text = Optional.ofNullable(transactionDetails.getProprietaryBankTransactionCode())
                .map(bankTransactionCode -> bankTransactionCode.split("\\+"))
                .map(array -> array.length > 2 ? array[1] : null)
                .map(BuchungstextMapper::gvcode2Buchungstext)
                .orElse(null);
            booking.setText(text);
        }

        BankAccount bankAccount = new BankAccount();

        // if amount < 0 the other account gets the money and is therefore the creditor
        // if amount > 0 we get the money and the other account is the debtor
        if (booking.getAmount() != null && BigDecimal.ZERO.compareTo(booking.getAmount()) > 0) { // 0 is bigger than amount
            bankAccount.setOwner(transactionDetails.getCreditorName());
            bankAccount.setIban(transactionDetails.getCreditorAccount() != null ? transactionDetails.getCreditorAccount().getIban() : null);
        } else {
            bankAccount.setOwner(transactionDetails.getDebtorName());
            bankAccount.setIban(transactionDetails.getDebtorAccount() != null ? transactionDetails.getDebtorAccount().getIban() : null);
        }
        booking.setOtherAccount(bankAccount);
        booking.setExternalId( // fallback external id if balance cannot be calculated
            Integer.toString(Objects.hash(
                booking.getBookingDate(),
                booking.getValutaDate(),
                booking.getAmount(),
                booking.getCurrency(),
                booking.getUsage(),
                transactionDetails.getEndToEndId(),
                booking.getTransactionCode(),
                booking.getOtherAccount().getOwner(),
                booking.getOtherAccount().getIban()
            ))
        );

        Optional.ofNullable(transactionDetails.getBalanceAfterTransaction())
            .map(de.adorsys.multibanking.xs2a_adapter.model.Balance::getBalanceAmount)
            .map(de.adorsys.multibanking.xs2a_adapter.model.Amount::getAmount)
            .map(BigDecimal::new)
            .ifPresent(balance -> booking.setBalance(balance));

        return booking;
    }

    @Mapping(target = "amount", source = "balanceAmount.amount")
    @Mapping(target = "date", source = "referenceDate")
    @Mapping(target = "currency", source = "balanceAmount.currency")
    Balance toBalance(de.adorsys.multibanking.xs2a_adapter.model.Balance balance);

    List<Message> toMessagesFromTppMessage400AIS(List<TppMessage400AIS> messages);

    List<Message> toMessages(List<MessageTO> messageTOList);

    @Mapping(target = "severity", source = "category")
    @Mapping(target = "key", source = "code")
    @Mapping(target = "renderedMessage", source = "text")
    @Mapping(target = "field", ignore = true)
    @Mapping(target = "paramsMap", ignore = true)
    Message toMessage(TppMessage400AIS tppMessage400AIS);

    ScaApproach toScaApproach(ResourceUpdateAuthResponseTO.ScaApproachEnum scaApproach);

    ScaStatus toScaStatus(ResourceUpdateAuthResponseTO.ScaStatusEnum scaStatus);

    @Mapping(target = "image", expression = "java(encodeBase64(challengeData.getImage()))")
    ChallengeData toChallengeData(ChallengeDataTO challengeData);

    default String encodeBase64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
}
