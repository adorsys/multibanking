import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContractBlacklistPage } from './contract-blacklist.page';

describe('ContractBlacklistPage', () => {
  let component: ContractBlacklistPage;
  let fixture: ComponentFixture<ContractBlacklistPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContractBlacklistPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContractBlacklistPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
