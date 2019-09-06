import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateConsentPage } from './create-consent.page';

describe('CreateConsentPage', () => {
  let component: CreateConsentPage;
  let fixture: ComponentFixture<CreateConsentPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateConsentPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateConsentPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
