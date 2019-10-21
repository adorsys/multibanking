import { TestBed } from '@angular/core/testing';

import { BookingResolverService } from './booking-resolver.service';

describe('BookingResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BookingResolverService = TestBed.get(BookingResolverService);
    expect(service).toBeTruthy();
  });
});
