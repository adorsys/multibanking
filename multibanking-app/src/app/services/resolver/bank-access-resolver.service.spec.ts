import { TestBed } from '@angular/core/testing';

import { BankAccessResolverService } from './bank-access-resolver.service';

describe('BankAccessResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BankAccessResolverService = TestBed.get(BankAccessResolverService);
    expect(service).toBeTruthy();
  });
});
