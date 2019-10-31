import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContractsPage } from './contracts.page';

describe('ContractsPage', () => {
  let component: ContractsPage;
  let fixture: ComponentFixture<ContractsPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContractsPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContractsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
