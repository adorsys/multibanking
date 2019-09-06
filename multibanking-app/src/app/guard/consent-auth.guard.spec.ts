import { TestBed, async, inject } from '@angular/core/testing';

import { ConsentAuthGuard } from './consent-auth.guard';

describe('ConsentAuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConsentAuthGuard]
    });
  });

  it('should ...', inject([ConsentAuthGuard], (guard: ConsentAuthGuard) => {
    expect(guard).toBeTruthy();
  }));
});
