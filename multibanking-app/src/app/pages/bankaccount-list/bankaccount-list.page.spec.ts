import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BankaccountListPage } from './bankaccount-list.page';

describe('BankaccountListPage', () => {
  let component: BankaccountListPage;
  let fixture: ComponentFixture<BankaccountListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BankaccountListPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BankaccountListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
