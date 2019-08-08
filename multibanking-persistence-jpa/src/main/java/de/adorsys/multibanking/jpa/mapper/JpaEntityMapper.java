package de.adorsys.multibanking.jpa.mapper;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.jpa.entity.*;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JpaEntityMapper {

    AccountAnalyticsEntity mapToAccountAnalyticsEntity(AccountAnalyticsJpaEntity accountAnalyticsEntity);

    @Mapping(source = "periodStart", target = "start")
    @Mapping(source = "periodEnd", target = "end")
    BookingPeriod mapToBookingPeriodEntity(BookingPeriodJpaEntity bookingPeriod);

    AccountAnalyticsJpaEntity mapToAccountAnalyticsJpaEntity(AccountAnalyticsEntity accountAnalyticsEntity);

    ContractJpaEntity mapToContractJpaEntity(ContractEntity contract);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    ContractJpaEntity mapToContractJpaEntity(Contract contract);

    @Mapping(target = "id", ignore = true)
    BookingGroupJpaEntity mapToBookingGroupJpaEntity(BookingGroup bookingGroup);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    BookingPeriodJpaEntity mapToBookingPeriodJpaEntity(BookingPeriod bookingPeriod);

    @Mapping(target = "id", ignore = true)
    ExecutedBookingJpaEntity mapToExecutedBookingJpaEntity(ExecutedBooking executedBooking);

    List<AnonymizedBookingJpaEntity> mapToAnonymizedBookingJpaEntities(List<AnonymizedBookingEntity> bookingEntities);

    BankAccessEntity mapToBankAccessEntity(BankAccessJpaEntity entity);

    List<TanTransportType> mapToTanTransportTypeList(List<TanTransportTypeJpaEntity> value);

    List<BankAccessEntity> mapToBankAccessEntities(List<BankAccessJpaEntity> byUserId);

    BankAccessJpaEntity mapToBankAccessJpaEntity(BankAccessEntity bankAccess);

    List<TanTransportTypeJpaEntity> mapToTanTransportTypeJpaEntityList(List<TanTransportType> value);

    List<BankAccountEntity> mapToBankAccountEntities(List<BankAccountJpaEntity> byUserId);

    BankAccountEntity mapToBankAccountEntity(BankAccountJpaEntity bankAccountJpaEntity);

    List<BankAccountJpaEntity> mapToBankAccountJpaEntities(List<BankAccountEntity> bankAccounts);

    BankAccountJpaEntity mapToBankAccountJpaEntity(BankAccountEntity bankAccount);

    @Mapping(source = "loginSettings.authType", target = "loginSettings.auth_type")
    @Mapping(target = "searchIndex", ignore = true)
    BankEntity mapToBankEntity(BankJpaEntity bankJpaEntity);

    List<BankJpaEntity> mapToBankJpaEntities(Iterable<BankEntity> bankEntities);

    @InheritInverseConfiguration
    BankJpaEntity mapToBankJpaEntity(BankEntity bank);

    @Mapping(target = "otherAccount.balances", ignore = true)
    @Mapping(target = "otherAccount.externalIdMap", ignore = true)
    BookingEntity mapToBookingEntity(BookingJpaEntity bookingJpaEntity);

    List<BookingEntity> mapToBookingEntities(List<BookingJpaEntity> valutaDate);

    List<BookingJpaEntity> mapToBookingJpaEntities(List<BookingEntity> newEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    BookingCategoryJpaEntity mapToBookingCategoryJpaEntity(BookingCategory bookingCategory);

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

    @Mapping(target = "psuAccount.balances", ignore = true)
    @Mapping(target = "psuAccount.externalIdMap", ignore = true)
    RawSepaTransactionEntity mapToRawSepaTransactionEntity(RawSepaTransactionJpaEntity rawSepaTransactionJpaEntity);

    @Mapping(target = "debtorBankAccount.id", ignore = true)
    @Mapping(target = "debtorBankAccount.bankAccessId", ignore = true)
    @Mapping(target = "debtorBankAccount.userId", ignore = true)
    RawSepaTransactionJpaEntity mapToRawSepaTransactionJpaEntity(RawSepaTransactionEntity paymentEntity);

    @Mapping(target = "psuAccount.balances", ignore = true)
    @Mapping(target = "psuAccount.externalIdMap", ignore = true)
    SinglePaymentEntity mapToPaymentEntity(SinglePaymentJpaEntity paymentJpaEntity);

    @Mapping(target = "debtorBankAccount.id", ignore = true)
    @Mapping(target = "debtorBankAccount.bankAccessId", ignore = true)
    @Mapping(target = "debtorBankAccount.userId", ignore = true)
    SinglePaymentJpaEntity mapToPaymentJpaEntity(SinglePaymentEntity paymentJpaEntity);

    @Mapping(target = "debtorBankAccount.id", ignore = true)
    @Mapping(target = "debtorBankAccount.bankAccessId", ignore = true)
    @Mapping(target = "debtorBankAccount.userId", ignore = true)
    StandingOrderJpaEntity mapToStandingOrderJpaEntity(StandingOrderEntity paymentJpaEntity);

    @Mapping(target = "psuAccount.balances", ignore = true)
    @Mapping(target = "psuAccount.externalIdMap", ignore = true)
    @Mapping(target = "otherAccount.balances", ignore = true)
    @Mapping(target = "otherAccount.externalIdMap", ignore = true)
    StandingOrderEntity mapToStandingOrderEntity(StandingOrderJpaEntity paymentJpaEntity);

    List<StandingOrderEntity> mapToStandingOrderEntities(List<StandingOrderJpaEntity> byUserIdAndAccountId);

    List<StandingOrderJpaEntity> mapToStandingOrderJpaEntities(List<StandingOrderEntity> standingOrders);

    UserEntity mapToUserEntity(UserJpaEntity userJpaEntity);

    UserJpaEntity mapToUserJpaEntity(UserEntity userEntity);

    List<BankEntity> mapToBankEntities(List<BankJpaEntity> bankJpaEntities);

    List<BookingsIndexEntity> mapToBookingsIndexEntities(List<BookingsIndexJpaEntity> bookingsIndexJpaEntities);
}
