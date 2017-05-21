package de.adorsys.multibanking.pers.spi.repository;

import domain.BankAccount;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BankAccountRepositoryCustom  {


    public BankAccount.SyncStatus getSyncStatus(String accountId);

    public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus);
}
