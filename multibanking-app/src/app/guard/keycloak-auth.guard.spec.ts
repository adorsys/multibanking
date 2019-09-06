import { TestBed, async, inject } from '@angular/core/testing';

import { KeycloakAuthGuard } from './keycloak-auth.guard';

describe('KeycloakAuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KeycloakAuthGuard]
    });
  });

  it('should ...', inject([KeycloakAuthGuard], (guard: KeycloakAuthGuard) => {
    expect(guard).toBeTruthy();
  }));
});
