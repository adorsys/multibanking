import { TestBed } from '@angular/core/testing';
import { FingerPrintService } from './fingerprint.service';



describe('FingerprintService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: FingerPrintService = TestBed.get(FingerprintService);
    expect(service).toBeTruthy();
  });
});
