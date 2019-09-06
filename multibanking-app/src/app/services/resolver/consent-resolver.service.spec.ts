import { TestBed } from '@angular/core/testing';

import { ConsentResolverService } from './consent-resolver.service';

describe('ConsentResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ConsentResolverService = TestBed.get(ConsentResolverService);
    expect(service).toBeTruthy();
  });
});
