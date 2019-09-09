import { TestBed } from '@angular/core/testing';

import { BankAccountsResolverService } from './bank-accounts-resolver.service';

describe('BankAccountsResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BankAccountsResolverService = TestBed.get(BankAccountsResolverService);
    expect(service).toBeTruthy();
  });
});
