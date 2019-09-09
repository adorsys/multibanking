import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorisationPage } from './authorisation.page';

describe('AuthorisationPage', () => {
  let component: AuthorisationPage;
  let fixture: ComponentFixture<AuthorisationPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AuthorisationPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthorisationPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
