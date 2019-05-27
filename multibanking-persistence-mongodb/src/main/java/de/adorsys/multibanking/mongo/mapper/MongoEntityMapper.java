package de.adorsys.multibanking.mongo.mapper;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.mongo.entity.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MongoEntityMapper {
    AccountAnalyticsEntity mapToAccountAnalyticsEntity(AccountAnalyticsMongoEntity accountAnalyticsEntity);

    AccountAnalyticsMongoEntity mapToAccountAnalyticsMongoEntity(AccountAnalyticsEntity accountAnalyticsEntity);

    List<AnonymizedBookingMongoEntity> mapToAnonymizedBookingMongoEntities(List<AnonymizedBookingEntity> bookingEntities);

    BankAccessEntity mapToBankAccessEntity(BankAccessMongoEntity entity);

    List<BankAccessEntity> mapToBankAccessEntities(List<BankAccessMongoEntity> byUserId);

    BankAccessMongoEntity mapToBankAccessMongoEntity(BankAccessEntity bankAccess);

    List<BankAccountEntity> mapToBankAccountEntities(List<BankAccountMongoEntity> byUserId);

    BankAccountEntity mapToBankAccountEntity(BankAccountMongoEntity bankAccountMongoEntity);

    List<BankAccountMongoEntity> mapToBankAccountMongoEntities(List<BankAccountEntity> bankAccounts);

    BankAccountMongoEntity mapToBankAccountMongoEntity(BankAccountEntity bankAccount);

    BankEntity mapToBankEntity(BankMongoEntity bankMongoEntity);

    List<BankMongoEntity> mapToBankMongoEntities(Iterable<BankEntity> bankEntities);

    BankMongoEntity mapToBankMongoEntity(BankEntity bank);

    BookingEntity mapToBookingEntity(BookingMongoEntity bookingMongoEntity);

    List<BookingEntity> mapToBookingEntities(List<BookingMongoEntity> valutaDate);

    List<BookingMongoEntity> mapToBookingMongoEntities(List<BookingEntity> newEntities);

    List<RuleEntity> mapToRuleEntities(List<RuleMongoEntity> byUserId);

    RuleEntity mapToRuleEntity(RuleMongoEntity ruleMongoEntity);

    RuleMongoEntity mapToRuleMongoEntity(RuleEntity ruleEntity);

    BookingsIndexMongoEntity mapToBookingsIndexMongoEntity(BookingsIndexEntity entity);

    BookingsIndexEntity mapToBookingsIndexEntity(BookingsIndexMongoEntity bookingsIndexMongoEntity);

    BulkPaymentMongoEntity mapToBulkPaymentMongoEntity(BulkPaymentEntity target);

    List<ContractEntity> mapToContractEntities(List<ContractMongoEntity> byUserIdAndAccountId);

    List<ContractMongoEntity> mapToContractMongoEntities(List<ContractEntity> contractEntities);

    MlAnonymizedBookingEntity mapToMlAnonymizedBookingEntity(MlAnonymizedBookingMongoEntity mlAnonymizedBookingMongoEntity);

    List<MlAnonymizedBookingEntity> mapToMlAnonymizedBookingEntities(List<MlAnonymizedBookingMongoEntity> byUserId);

    MlAnonymizedBookingMongoEntity mapToMlAnonymizedBookingMongoEntity(MlAnonymizedBookingEntity booking);

    RawSepaTransactionEntity mapToRawSepaTransactionEntity(RawSepaTransactionMongoEntity rawSepaTransactionMongoEntity);

    RawSepaTransactionMongoEntity mapToRawSepaTransactionMongoEntity(RawSepaTransactionEntity paymentEntity);

    PaymentEntity mapToPaymentEntity(PaymentMongoEntity paymentMongoEntity);

    PaymentMongoEntity mapToPaymentMongoEntity(PaymentEntity paymentMongoEntity);

    StandingOrderMongoEntity mapToStandingOrderMongoEntity(StandingOrderEntity paymentMongoEntity);

    List<StandingOrderEntity> mapToStandingOrderEntities(List<StandingOrderMongoEntity> byUserIdAndAccountId);

    List<StandingOrderMongoEntity> mapToStandingOrderMongoEntities(List<StandingOrderEntity> standingOrders);

    UserEntity mapToUserEntity(UserMongoEntity userMongoEntity);

    UserMongoEntity mapToUserMongoEntity(UserEntity userEntity);

    List<BankEntity> mapToBankEntities(List<BankMongoEntity> bankMongoEntities);

    List<BookingsIndexEntity> mapToBookingsIndexEntities(List<BookingsIndexMongoEntity> bookingsIndexMongoEntities);
}
