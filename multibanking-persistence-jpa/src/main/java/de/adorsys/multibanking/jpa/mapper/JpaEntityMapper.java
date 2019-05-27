package de.adorsys.multibanking.jpa.mapper;

import de.adorsys.multibanking.jpa.entity.*;
import de.adorsys.multibanking.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JpaEntityMapper {

    AccountAnalyticsEntity mapToAccountAnalyticsEntity(AccountAnalyticsJpaEntity accountAnalyticsEntity);

    AccountAnalyticsJpaEntity mapToAccountAnalyticsJpaEntity(AccountAnalyticsEntity accountAnalyticsEntity);

    List<AnonymizedBookingJpaEntity> mapToAnonymizedBookingJpaEntities(List<AnonymizedBookingEntity> bookingEntities);

    BankAccessEntity mapToBankAccessEntity(BankAccessJpaEntity entity);

    List<BankAccessEntity> mapToBankAccessEntities(List<BankAccessJpaEntity> byUserId);

    BankAccessJpaEntity mapToBankAccessJpaEntity(BankAccessEntity bankAccess);

    List<BankAccountEntity> mapToBankAccountEntities(List<BankAccountJpaEntity> byUserId);

    BankAccountEntity mapToBankAccountEntity(BankAccountJpaEntity bankAccountJpaEntity);

    List<BankAccountJpaEntity> mapToBankAccountJpaEntities(List<BankAccountEntity> bankAccounts);

    BankAccountJpaEntity mapToBankAccountJpaEntity(BankAccountEntity bankAccount);

    BankEntity mapToBankEntity(BankJpaEntity bankJpaEntity);

    List<BankJpaEntity> mapToBankJpaEntities(Iterable<BankEntity> bankEntities);

    BankJpaEntity mapToBankJpaEntity(BankEntity bank);

    BookingEntity mapToBookingEntity(BookingJpaEntity bookingJpaEntity);

    List<BookingEntity> mapToBookingEntities(List<BookingJpaEntity> valutaDate);

    List<BookingJpaEntity> mapToBookingJpaEntities(List<BookingEntity> newEntities);

    List<RuleEntity> mapToRuleEntities(List<RuleJpaEntity> byUserId);

    RuleEntity mapToRuleEntity(RuleJpaEntity ruleJpaEntity);

    RuleJpaEntity mapToRuleJpaEntity(RuleEntity ruleEntity);

    BookingsIndexJpaEntity mapToBookingsIndexJpaEntity(BookingsIndexEntity entity);

    BookingsIndexEntity mapToBookingsIndexEntity(BookingsIndexJpaEntity bookingsIndexJpaEntity);

    BulkPaymentJpaEntity mapToBulkPaymentJpaEntity(BulkPaymentEntity target);

    List<ContractEntity> mapToContractEntities(List<ContractJpaEntity> byUserIdAndAccountId);

    List<ContractJpaEntity> mapToContractJpaEntities(List<ContractEntity> contractEntities);

    MlAnonymizedBookingEntity mapToMlAnonymizedBookingEntity(MlAnonymizedBookingJpaEntity mlAnonymizedBookingJpaEntity);

    List<MlAnonymizedBookingEntity> mapToMlAnonymizedBookingEntities(List<MlAnonymizedBookingJpaEntity> byUserId);

    MlAnonymizedBookingJpaEntity mapToMlAnonymizedBookingJpaEntity(MlAnonymizedBookingEntity booking);

    RawSepaTransactionEntity mapToRawSepaTransactionEntity(RawSepaTransactionJpaEntity rawSepaTransactionJpaEntity);

    RawSepaTransactionJpaEntity mapToRawSepaTransactionJpaEntity(RawSepaTransactionEntity paymentEntity);

    PaymentEntity mapToPaymentEntity(PaymentJpaEntity paymentJpaEntity);

    PaymentJpaEntity mapToPaymentJpaEntity(PaymentEntity paymentJpaEntity);

    StandingOrderJpaEntity mapToStandingOrderJpaEntity(StandingOrderEntity paymentJpaEntity);

    List<StandingOrderEntity> mapToStandingOrderEntities(List<StandingOrderJpaEntity> byUserIdAndAccountId);

    List<StandingOrderJpaEntity> mapToStandingOrderJpaEntities(List<StandingOrderEntity> standingOrders);

    UserEntity mapToUserEntity(UserJpaEntity userJpaEntity);

    UserJpaEntity mapToUserJpaEntity(UserEntity userEntity);

    List<BankEntity> mapToBankEntities(List<BankJpaEntity> bankJpaEntities);

    List<BookingsIndexEntity> mapToBookingsIndexEntities(List<BookingsIndexJpaEntity> bookingsIndexJpaEntities);
}
