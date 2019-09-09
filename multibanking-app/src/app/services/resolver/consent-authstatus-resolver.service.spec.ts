import { TestBed } from '@angular/core/testing';

import { ConsentAuthstatusResolverService } from './consent-authstatus-resolver.service';

describe('ConsentAuthstatusResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ConsentAuthstatusResolverService = TestBed.get(ConsentAuthstatusResolverService);
    expect(service).toBeTruthy();
  });
});
