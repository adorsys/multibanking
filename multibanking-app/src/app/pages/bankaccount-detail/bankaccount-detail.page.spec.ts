import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BankaccountDetailPage } from './bankaccount-detail.page';

describe('BookingTabsPage', () => {
  let component: BankaccountDetailPage;
  let fixture: ComponentFixture<BankaccountDetailPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BankaccountDetailPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BankaccountDetailPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
